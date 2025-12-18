from config.dependencies import container

enricher = container.enricher

# [{country_name}, {country_name, state_name}, {country_name, state_name, city_name}, {country_name, city_name}]
test_cases = [
    {"country_name": "United States Virgin Islands"},
    {"country_name": "Hong Kong"},
    {"country_name": "Vatican City"},
    {"country_name": "Bahamas"},
    {"country_name": "Saint Martin"},
    {"country_name": "United States", "state_name": "California"},
    {"country_name": "Japan", "city_name": "Tokyo"},
    {"country_name": "Philippines", "city_name": "Manila"},
    {"country_name": "Philippines", "city_name": "Makati"},
    {"country_name": "Sweden", "city_name": "Gothenburg"},
]

failed_cases = [
  { "country_name": "Colombia", "state_name": None, "city_name": "Cartagena" },
  { "country_name": "Turkey", "state_name": None, "city_name": "Ephesus" }, # 관광지
  { "country_name": "Vietnam", "state_name": None, "city_name": "Ha noi" },
  { "country_name": "Egypt", "state_name": None, "city_name": "Saqqara" }, # 관광지
  { "country_name": "Morocco", "state_name": None, "city_name": "Fez" },
  { "country_name": "Chile", "state_name": None, "city_name": "Punta de Choros" }, # 관광지
  { "country_name": "Vietnam", "state_name": None, "city_name": "Saigon" }, # 옛이름
  { "country_name": "Turkey", "state_name": None, "city_name": "Cappadocia" }, # 관광지
  { "country_name": "Bahamas", "state_name": None, "city_name": "Cabbage Beach" }, # 관광지
  { "country_name": "Jordan", "state_name": None, "city_name": "Wadi Rum" }, # 관광지
  { "country_name": "United States", "state_name": "Hawaii", "city_name": "Waikiki"},  # 관광지인데 그러면 country,state가 enrich돼야 하는데 state만 되었음
  { "country_name": "El Salvador", "state_name": "La Libertad", "city_name": "El Tunco"}, # 관광지인데 그러면 country,state가 enrich돼야 하는데 state만 되었음
]

print("=" * 80)
print("Location Enrichment Test Results")
print("=" * 80)

for i, test_case in enumerate(failed_cases, 1):
    country_name = test_case.get("country_name")
    state_name = test_case.get("state_name")
    city_name = test_case.get("city_name")

    print(f"\n[Test Case {i}]")
    print(f"Input: ", end="")

    # 입력 출력
    parts = []
    if city_name:
        parts.append(f"City={city_name}")
    if state_name:
        parts.append(f"State={state_name}")
    if country_name:
        parts.append(f"Country={country_name}")
    print(", ".join(parts) if parts else "Empty")

    # Enrich 실행
    location = enricher.enrich_location(country_name, state_name, city_name)

    # 결과 출력
    if location:
        print(f"Result:")

        country_id = location.get("country_id")
        state_id = location.get("state_id")
        city_id = location.get("city_id")
        print(f"Country ID: {country_id}")
        print(f"State ID: {state_id}")
        print(f"City ID: {city_id}")

    else:
        print(f"Result: ✗ No match found")

print("\n" + "=" * 80)