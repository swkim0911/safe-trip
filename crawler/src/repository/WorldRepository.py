from adapters import mongo_client
from pymongo import InsertOne


class WorldRepository:
    def __init__(self):
        self.country_collection = mongo_client.get_country_collection()
        self.state_collection = mongo_client.get_state_collection()
        self.city_collection = mongo_client.get_city_collection()

    def __find_city(self, query):
        doc = self.city_collection.find_one(
            query,
            {"_id": 1, "country_id": 1, "state_id": 1},
            collation={"locale": "en", "strength": 1},
        )
        if not doc:
            return None
        doc["city_id"] = doc.pop("_id")
        return doc

    def __find_state(self, query):
        doc = self.state_collection.find_one(
            query,
            {"_id": 1, "country_id": 1},
            collation={"locale": "en", "strength": 1},
        )
        if not doc:
            return None
        doc["state_id"] = doc.pop("_id")
        return doc

    def __find_country(self, query):
        doc = self.country_collection.find_one(
            query,
            {"_id": 1},
            collation={"locale": "en", "strength": 1},
        )
        if not doc:
            return None
        doc["country_id"] = doc.pop("_id")
        return doc

    def lookup_location(self, country_name, state_name, city_name):
        queries = {
            "country:state:city": {"name": city_name, "state_name": state_name, "country_name": country_name},
            "country:city": {"name": city_name, "country_name": country_name},
            "state:city": {"name": city_name, "state_name": state_name},
            "country:state": {"name": state_name, "country_name": country_name},
            "city": {"name": city_name},
            "state": {"name": state_name},
            "country": {"name": country_name},
        }

        match (bool(country_name), bool(state_name), bool(city_name)):
            case (True, True, True):
                return (
                    self.__find_city(queries["country:state:city"])
                    or self.__find_city(queries["country:city"])
                    or self.__find_city(queries["state:city"])
                    or self.__find_state(queries["country:state"])
                    or self.__find_city(queries["city"])
                    or self.__find_state(queries["state"])
                    or self.__find_country(queries["country"])
                )
            case (True, True, False):
                return (
                    self.__find_state(queries["country:state"])
                    or self.__find_state(queries["state"])
                    or self.__find_country(queries["country"])
                )
            case (True, False, True):
                return (
                    self.__find_city(queries["country:city"])
                    or self.__find_city(queries["city"])
                    or self.__find_country(queries["country"])
                )
            case (False, True, True):
                return (
                    self.__find_city(queries["state:city"])
                    or self.__find_city(queries["city"])
                    or self.__find_state(queries["state"])
                )
            case (True, False, False):
                return self.__find_country(queries["country"])
            case (False, True, False):
                return self.__find_state(queries["state"])
            case (False, False, True):
                return self.__find_city(queries["city"])
            case _:
                return None
