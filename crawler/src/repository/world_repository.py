class WorldRepository:
    def __init__(self, country_collection, state_collection, city_collection):
        self.country_collection = country_collection
        self.state_collection = state_collection
        self.city_collection = city_collection

    def find_city(self, query):
        doc = self.city_collection.find_one(
            query,
            {"_id": 1, "country_id": 1, "state_id": 1},
            collation={"locale": "en", "strength": 1}, # 대소문자, 악센트, 기호 무시 (A == a == Á == ä)
        )
        
        if not doc:
            return None
        doc["city_id"] = doc.pop("_id")
        return doc

    def find_state(self, query):
        doc = self.state_collection.find_one(
            query,
            {"_id": 1, "country_id": 1},
            collation={"locale": "en", "strength": 1},
        )
        
        if not doc:
            return None
        doc["state_id"] = doc.pop("_id")
        return doc

    def find_country(self, query):
        doc = self.country_collection.find_one(
            query,
            {"_id": 1},
            collation={"locale": "en", "strength": 1},
        )
        
        if not doc:
            return None
        doc["country_id"] = doc.pop("_id")
        return doc

    def get_all_countries(self):
        return self.country_collection.find(
            {},
            {"_id": 1, "name": 1, "native": 1}
        )

    def lookup_location(self, country_name, state_name, city_name):
        match (bool(country_name), bool(state_name), bool(city_name)):
            case (True, True, True):
                return self.find_city({
                    "name": city_name,
                    "state_name": state_name,
                    "country_name": country_name,
                }) or self.find_city({
                    "native": city_name,
                    "state_name": state_name,
                    "country_name": country_name,
                })

            case (True, True, False):
                return self.find_state({
                    "name": state_name,
                    "country_name": country_name,
                }) or self.find_state({
                    "native": state_name,
                    "country_name": country_name,
                })

            case (True, False, True):
                return self.find_city({
                    "name": city_name,
                    "country_name": country_name,
                }) or self.find_city({
                    "native": city_name,
                    "country_name": country_name,
                })

            case (False, True, True):
                return self.find_city({
                    "name": city_name,
                    "state_name": state_name,
                }) or self.find_city({
                    "native": city_name,
                    "state_name": state_name,
                })

            case (True, False, False):
                return self.find_country({
                    "name": country_name,
                }) or self.find_country({
                    "native": country_name,
                })

            case (False, True, False):
                return self.find_state({
                    "name": state_name,
                }) or self.find_state({
                    "native": state_name,
                })

            case (False, False, True):
                return self.find_city({
                    "name": city_name,
                }) or self.find_city({
                    "native": city_name,
                })

            case _:
                return None

