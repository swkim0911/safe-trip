class WorldRepository:
    def __init__(self, country_collection, state_collection, city_collection):
        self.country_collection = country_collection
        self.state_collection = state_collection
        self.city_collection = city_collection

    def lookup_location(self, country_name, state_name, city_name):
        """
        - city_name으로 조회가 된다면, 사용자는 (city, state, country) 전체 정보(디테일한 정보)를 알 수 있어서 city_name이 가장 가치있는 데이터이다.
        - 하지만 city_name을 포함해서 실패했다는 건:
            - city가 오타거나
            - DB에 동일한게 없거나
            - 표기 문제가 있다는 뜻
        - 그런데 이 시점에 state/country로 내려가 버리면 city 정보를 버려버린다.
        - 따라서 city_name으로 조회하지 못하면 바로 return을 하고, 이후에 fuzzy_match 단계에서 활용해서 최대한 city 정보를 이용한다.
        """
        if city_name:
            return (
                    self._find_city_by_name(city_name, state_name, country_name)
                    or self._find_city_by_native(city_name, state_name, country_name)
            )

        if state_name:
            return (
                    self._find_state_by_name(state_name, country_name)
                    or self._find_state_by_native(state_name, country_name)
            )

        if country_name:
            return (
                    self._find_country_by_name(country_name)
                    or self._find_country_by_native(country_name)
            )

        return None

    def get_all_countries(self):
        return self.country_collection.find(
            {},
            {"_id": 1, "name": 1, "native": 1, "population": 1}
        )

    def get_cities_by_country_id(self, country_id):
        return self.city_collection.find(
            {"country_id": country_id},
            {"_id": 1, "name": 1, "native": 1, "population": 1}
        )

    def get_states_by_country_id(self, country_id):
        return self.state_collection.find(
            {"country_id": country_id},
            {"_id": 1, "name": 1, "native": 1, "population": 1}
        )

    def _find_city_by_name(self, city_name, state_name=None, country_name=None):
        query = self._build_city_query(
            field="name",
            value=city_name,
            state_name=state_name,
            country_name=country_name,
        )
        return self._find_city(query)

    def _find_city_by_native(self, city_name, state_name=None, country_name=None):
        query = self._build_city_query(
            field="native",
            value=city_name,
            state_name=state_name,
            country_name=country_name,
        )
        return self._find_city(query)

    def _find_state_by_name(self, state_name, country_name=None):
        query = self._build_state_query(
            field="name",
            value=state_name,
            country_name=country_name,
        )
        return self._find_state(query)

    def _find_state_by_native(self, state_name, country_name=None):
        query = self._build_state_query(
            field="native",
            value=state_name,
            country_name=country_name,
        )
        return self._find_state(query)

    def _find_country_by_name(self, country_name):
        query = self._build_country_query(
            field="name",
            value=country_name
        )
        return self._find_country(query)

    def _find_country_by_native(self, country_name):
        query = self._build_country_query(
            field="native",
            value=country_name
        )
        return self._find_country(query)

    def _find_city(self, query):
        doc = self.city_collection.find_one(
            query,
            {"_id": 1, "country_id": 1, "state_id": 1},
            collation={"locale": "en", "strength": 1}, # 대소문자, 악센트, 기호 무시 (A == a == Á == ä)
        )
        
        if not doc:
            return None
        doc["city_id"] = doc.pop("_id")
        return doc

    def _find_state(self, query):
        doc = self.state_collection.find_one(
            query,
            {"_id": 1, "country_id": 1},
            collation={"locale": "en", "strength": 1},
        )
        
        if not doc:
            return None
        doc["state_id"] = doc.pop("_id")
        return doc

    def _find_country(self, query):
        doc = self.country_collection.find_one(
            query,
            {"_id": 1},
            collation={"locale": "en", "strength": 1},
        )
        
        if not doc:
            return None
        doc["country_id"] = doc.pop("_id")
        return doc

    def _build_city_query(
            self,
            *,
            field: str,
            value: str,
            state_name=None,
            country_name=None
    ):
        query = {field: value}

        if state_name:
            query["state_name"] = state_name
        if country_name:
            query["country_name"] = country_name

        return query

    def _build_state_query(
            self,
            *,
            field: str,
            value: str,
            country_name=None
    ):
        query = {field: value}

        if country_name:
            query["country_name"] = country_name

        return query

    def _build_country_query(
            self,
            *,
            field: str,
            value: str
    ):
        query = {field: value}

        return query

