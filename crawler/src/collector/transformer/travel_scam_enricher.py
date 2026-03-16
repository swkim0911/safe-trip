import logging
from unidecode import unidecode
from utils.fuzzy_utils import get_fuzz_ratio

class TravelScamEnricher:
    """Location 정보를 DB lookup하여 enrichment하는 클래스"""

    _FUZZY_THRESHOLD = 75
    
    def __init__(self, world_repository):
        self.world_repository = world_repository
        self.logger = logging.getLogger(__name__)

    def normalize(self, text):
        """
        부분 문자열 비교용
        """
        if not text:
            return None
        return unidecode(text).lower().strip()

    def normalize_compact(self, text):
        """
        공백까지 제거한 정규화 (fuzzy 비교용)
        """
        if not text:
            return None
        return unidecode(text).lower().replace(" ", "").strip()

    def enrich_location(self, country_name: str | None, state_name: str | None, city_name: str | None) -> dict | None:
        """
        Location 정보를 DB에서 lookup하여 enrichment 한다.

        Returns:
        dict | None:
            - 성공 시: 확정된 location 식별자만 포함한 dict
              (예: {"country_id": 1},
                   {"country_id": 1, "state_id": 10},
                   {"country_id": 1, "state_id": 10, "city_id": 100})
            - 실패 시: None
        """
        # 0단계: 지역 이름 정규화
        country_name = self.normalize(country_name) if country_name else None
        state_name = self.normalize(state_name) if state_name else None
        city_name = self.normalize(city_name) if city_name else None

        # 위치 정보가 모두 없다면 None 반환
        if not country_name and not state_name and not city_name:
            self.logger.debug("country_name, state_name, city_name이 없어서 enrichment 불가")
            return None
        
        # 1단계: parsing된 location 정보가 정확히 매치되는지 확인
        location = self._try_exact_match(country_name, state_name, city_name)
        if location:
            self.logger.debug(f"정확한 매칭 성공: {country_name}, {state_name}, {city_name}")
            return location
        
        # 2단계: city/state가 바뀐 경우 시도 (state, city) -> (state=city, city=state), (state, null) -> (state=null, city=state), (null, city) -> (state=city, city=null)
        if state_name or city_name:
            location = self._try_swapped(country_name, state_name, city_name)
            if location:
                self.logger.debug(f"교환 매칭 성공: {country_name}, {state_name}, {city_name}")
                return location

        # 3단계: country-only remapping (country만 있고, state, city가 없을 때 country를 state 혹은 city 재배치해서 시도)
        if country_name and not state_name and not city_name:
            location = self._try_country_only_remap(country_name)
            if location:
                self.logger.debug(f"country-only remap 성공: '{country_name}' → state/city")
                return location


        # 4단계: 이제는 문자열 정확하게는 찾을 수 없으므로 fuzzy match(유사도)으로 찾기
        if country_name:
            location = self._try_fuzzy(country_name, state_name, city_name)
            if location:
                self.logger.debug(f"Fuzzy 매칭 성공: {location}")
                return location

            self.logger.warning(f"Fuzzy 매칭 실패: country={country_name}, state={state_name}, city={city_name}")
            return None

        # consider: 프롬프트에 country 필드가 NULL인 경우는 요구하지 않아서 country_name=NULL인 경우는 거의 없다.(99%) 그래도 나중에 필요하면 처리 로직을 구현한다.

        
        self.logger.warning(f"매칭 실패: country={country_name}, state={state_name}, city={city_name}")
        return None
    
    def _try_exact_match(self, country_name: str, state_name: str | None, city_name: str | None) -> dict | None:
        """LLM API가 parsing한 location 정보와 정확한 매칭 시도"""
        return self.world_repository.lookup_location(country_name, state_name, city_name)
    
    def _try_swapped(self, country_name: str, state_name: str, city_name: str) -> dict | None:
        """city와 state가 바뀐 경우 시도"""
        location = self.world_repository.lookup_location(country_name, city_name, state_name)
        return location

    def _try_country_only_remap(self, country_name: str) -> dict | None:
        """country만 있는 경우, state, city에 매핑 시도"""
        return self.world_repository.lookup_location(None, None, country_name) or self.world_repository.lookup_location(None, country_name, None)

    def _try_fuzzy(self, country_name, state_name, city_name):
        # 1. 국가 이름에 매핑되는 국가 찾기
        country = self._fuzzy_match_country(country_name) # {country_id: XX}

        if not country:
            return None

        if not state_name and not city_name:
            return country

        if state_name and city_name:
            location = self._fuzzy_match_city(country, city_name, state_name)
            if location:
                return location

            # 3. country + city
        if city_name:
            location = self._fuzzy_match_city(country, city_name)
            if location:
                return location

            # 4. country + state
        if state_name:
            location = self._fuzzy_match_state(country, state_name)
            if location:
                return location

        return country

    def _fuzzy_match_country(self, target_country_name: str):
        """
        주어진 country_name(normalized 상태)과 가장 유사한 국가를 찾는다.
        Returns:
            {country_id: X} 또는 None
        """
        all_countries = list(self.world_repository.get_all_countries())

        return self._fuzzy_match_entity(
            entities=all_countries,
            target_name=target_country_name,
            id_key="country_id"
        )

    def _fuzzy_match_state(self, country, target_state_name):
        """
        주어진 state_name(normalized 상태)과 가장 유사한 state(주)를 찾는다.
        Returns:
            {state_id: X} 또는 None
        """
        country_id = country["country_id"]
        all_states = list(self.world_repository.get_states_by_country_id(country_id))

        return self._fuzzy_match_entity(
            entities=all_states,
            target_name=target_state_name,
            id_key="state_id",
            additional_fields=["country_id"]
        )

    def _fuzzy_match_city(self, country, target_city_name, target_state_name=None):
        """
        주어진 target_city_name과 가장 유사한 도시를 찾는다.
        target_state_name이 있으면 더 정확한 매칭이 가능하다.

        Returns:
            {city_id: X, state_id: Y} 또는 None
        """
        country_id = country["country_id"]
        all_cities = list(self.world_repository.get_cities_by_country_id(country_id))

        # state 정보가 있는 경우
        if target_state_name:
            return self._fuzzy_match_city_with_state(
                all_cities=all_cities,
                target_city_name=target_city_name,
                target_state_name=target_state_name,
                fuzzy_threshold=self._FUZZY_THRESHOLD
            )

        # state 정보가 없으면 기본 매칭
        result = self._fuzzy_match_entity(
            entities=all_cities,
            target_name=target_city_name,
            id_key="city_id",
            additional_fields=["state_id", "country_id"]
        )

        return result

    def _fuzzy_match_city_with_state(self, all_cities, target_city_name, target_state_name, fuzzy_threshold):
        """
        State 정보를 활용하여 City를 더 정확하게 매칭한다.

        전략:
        1. cities의 state_id로 states를 조회하여 매칭되는 state_id 목록을 얻음
        2. 해당 state_id를 가진 cities만 필터링하여 city 매칭
        3. 실패하면 state 정보를 무시하고 전체 cities에서 재시도
        ex) Haikou, Hainan, China vs Haikou, Yunnan, China

        Returns:
            {city_id: X, state_id: Y} 또는 None
        """
        # Step 1: 먼저 state 매칭하여 가능한 state_id들을 얻기
        state_ids = self._get_matching_state_ids(all_cities, target_state_name, fuzzy_threshold)

        if state_ids:
            # Step 2: 해당 state들에 속한 cities만 필터링
            filtered_cities = [city for city in all_cities if city["state_id"] in state_ids]

            if filtered_cities:
                # Step 3: 필터링된 cities 내에서 city 매칭
                result = self._fuzzy_match_entity(
                    entities=filtered_cities,
                    target_name=target_city_name,
                    id_key="city_id",
                    additional_fields=["state_id", "country_id"]
                )

                if result:
                    return result

        # Step 4: state 필터링으로 못 찾으면 전체에서 재시도
        result = self._fuzzy_match_entity(
            entities=all_cities,
            target_name=target_city_name,
            id_key="city_id",
            additional_fields=["state_id", "country_id"]
        )

        return result

    def _get_matching_state_ids(self, all_cities, target_state_name, fuzzy_threshold):
        """
        cities 리스트에서 고유한 state_id들을 추출한 뒤 DB에서 states를 조회하여
        매칭되는 state_id들을 반환한다.

        Returns:
            list: 매칭된 state_id 리스트
        """
        unique_state_ids = list({city["state_id"] for city in all_cities if city.get("state_id")})
        states = list(self.world_repository.get_states_by_ids(unique_state_ids))
        if not states:
            return []

        # 1차: 정확하게 일치하는 state 검색
        for state in states:
            if (target_state_name == self.normalize(state.get("name", "")) or
                    target_state_name == self.normalize(state.get("native", ""))):
                return [state["_id"]]  # 즉시 반환

        # 2차: 부분 일치 및 fuzzy 검색
        matching_state_ids = []
        for state in states:
            state_name = self.normalize(state.get("name", ""))
            state_native = self.normalize(state.get("native", ""))

            if (target_state_name in state_name or target_state_name in state_native or
                    get_fuzz_ratio(state_name, target_state_name) > fuzzy_threshold or
                    get_fuzz_ratio(state_native, target_state_name) > fuzzy_threshold):
                matching_state_ids.append(state["_id"])

        return matching_state_ids

    def _fuzzy_match_entity(self, entities, target_name, id_key, additional_fields=None):
        """
        공통 fuzzy matching 로직.

        Args:
            entities: 검색할 엔티티 리스트 (country, state, city 등)
            target_name: 찾고자 하는 이름 (normalized)
            id_key: 결과에 포함할 ID 키 ("country_id" or "state_id" or "city_id")
            additional_fields: 추가로 결과에 포함할 필드 리스트 (예: ["state_id"])

        Returns:
           {"country_id": X} or
           {"country_id": X, "state_id": X} or
           {"country_id": X, "state_id": X, "city_id": X}or None
        """
        # name 필드로 먼저 시도
        result = self._match_by_field(
            entities=entities,
            target_name=target_name,
            field_name="name",
            id_key=id_key,
            additional_fields=additional_fields
        )
        if result:
            return result

        # native 필드로 시도
        result = self._match_by_field(
            entities=entities,
            target_name=target_name,
            field_name="native",
            id_key=id_key,
            additional_fields=additional_fields
        )
        if result:
            return result

        return None

    def _match_by_field(self, entities, target_name, field_name, id_key, additional_fields=None):
        """
        특정 필드(name 또는 native)를 기준으로 엔티티를 매칭한다.

        Args:
            entities: 전체 엔티티 리스트
            target_name: 찾고자 하는 이름 (normalized)
            field_name: 비교할 필드명 ("name" 또는 "native")
            id_key: 결과에 포함할 ID 키
            additional_fields: 추가로 결과에 포함할 필드 리스트

        """
        # 1. 부분 일치하는 엔티티 찾기
        candidates = self._find_partial_matches(entities, target_name, field_name)
        result = self._select_best_candidate(candidates, id_key, additional_fields)
        if result:
            return result

        # 2. fuzzy match로 후보 엔티티 찾기
        candidates = self._find_fuzzy_matches(entities, target_name, field_name, self._FUZZY_THRESHOLD)
        result = self._select_best_candidate(candidates, id_key, additional_fields)
        if result:
            return result

        return None

    def _find_partial_matches(self, entities, target_name, field_name):
        """부분 일치하는 엔티티들을 찾는다."""
        candidates = []
        for entity in entities:
            entity_name = self.normalize(entity.get(field_name, ""))
            if target_name in entity_name:
                candidates.append({
                    "id": entity["_id"],
                    "name": entity_name,
                    "population": entity.get("population"),
                    "state_id": entity.get("state_id"),
                    "country_id": entity.get("country_id")
                })
        return candidates

    def _find_fuzzy_matches(self, entities, target_name, field_name, fuzzy_threshold):
        """fuzzy matching으로 유사한 엔티티들을 찾는다."""
        target_name = self.normalize_compact(target_name)
        candidates = []

        for entity in entities:
            entity_name = self.normalize_compact(entity.get(field_name, ""))
            ratio = get_fuzz_ratio(entity_name, target_name)

            if len(target_name) == 3 and len(entity_name) == 3:
                fuzzy_threshold = 66

            if ratio > fuzzy_threshold:
                candidates.append({
                    "id": entity["_id"],
                    "name": entity_name,
                    "population": entity.get("population"),
                    "state_id": entity.get("state_id"),
                    "country_id": entity.get("country_id")
                })
        return candidates

    def _select_best_candidate(self, candidates, id_key, additional_fields=None):
        """
        후보 중에서 최적의 엔티티를 선택한다.
        - 후보가 1개면 그것을 반환
        - 후보가 여러 개면 인구수가 가장 많은 것을 반환 (인구수가 많다는 것은 가장 활발한 지역, 즉 관광지일 확률이 높다)

        Args:
            candidates: 후보 리스트
            id_key: 결과에 사용할 ID 키명 ("country_id", "state_id", "city_id")
            additional_fields: 추가로 결과에 포함할 필드 리스트

        Returns:
            {id_key: X, ...additional_fields} 또는 None
        """
        if len(candidates) == 0:
            return None

        if len(candidates) == 1:
            return self._build_result(candidates[0], id_key, additional_fields)

        # 여러 후보 중 인구수가 가장 많은 것 선택
        max_candidate = max(candidates, key=lambda x: x["population"] or 0)
        if max_candidate["population"] is not None:
            return self._build_result(max_candidate, id_key, additional_fields)

        return self._build_result(candidates[0], id_key, additional_fields) # 인구수로 판단할 수 없는 경우 db에서 먼저 나오는 것으로 처리


    def _build_result(self, candidate, id_key, additional_fields=None):
        """
        후보로부터 결과 딕셔너리를 구성한다.

        Args:
            candidate: 선택된 후보
            id_key: 결과에 사용할 ID 키명
            additional_fields: 추가로 포함할 필드 리스트

        Returns:
            {id_key: X, ...additional_fields}
        """
        result = {id_key: candidate["id"]}

        if additional_fields:
            for field in additional_fields:
                if field in candidate and candidate[field] is not None:
                    result[field] = candidate[field]

        return result