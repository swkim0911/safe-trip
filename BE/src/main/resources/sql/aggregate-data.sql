REPLACE INTO ext_country_stats SELECT country_id, COUNT(*) FROM external_report GROUP BY country_id;
REPLACE INTO ext_state_stats SELECT state_id, COUNT(*) FROM external_report WHERE state_id IS NOT NULL GROUP BY state_id;
REPLACE INTO ext_city_stats SELECT city_id, COUNT(*) FROM external_report WHERE city_id IS NOT NULL GROUP BY city_id;
REPLACE INTO ext_scam_action_stats SELECT 0, scam_action_id, COUNT(*) FROM external_report WHERE scam_action_id IS NOT NULL GROUP BY scam_action_id;
REPLACE INTO ext_scam_action_stats SELECT country_id, scam_action_id, COUNT(*) FROM external_report WHERE scam_action_id IS NOT NULL GROUP BY country_id, scam_action_id;
REPLACE INTO ext_scam_context_stats SELECT 0, scam_context_id, COUNT(*) FROM external_report WHERE scam_context_id IS NOT NULL GROUP BY scam_context_id;
REPLACE INTO ext_scam_context_stats SELECT country_id, scam_context_id, COUNT(*) FROM external_report WHERE scam_context_id IS NOT NULL GROUP BY country_id, scam_context_id;
