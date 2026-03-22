SHELL := /bin/bash
.PHONY: dev infra stop be fe stop-be stop-fe

# 전체 기동: docker + BE + FE 병렬 실행 (Ctrl+C로 BE+FE 종료)
dev:
	docker compose -f docker-compose.dev.yml up -d
	@trap 'kill %1 %2 2>/dev/null; exit 0' INT; \
	(cd BE && ./gradlew bootRun) & \
	(cd FE && npm run dev) & \
	wait

# BE만 기동
be:
	cd BE && ./gradlew bootRun

# FE만 기동
fe:
	cd FE && npm run dev

# docker 인프라만 기동
infra:
	docker compose -f docker-compose.dev.yml up -d

# BE만 종료
stop-be:
	-pkill -f 'bootRun'

# FE만 종료
stop-fe:
	-pkill -f 'vite'

# 전체 종료 (BE + FE + docker)
stop:
	-pkill -f 'bootRun'
	-pkill -f 'vite'
	docker compose -f docker-compose.dev.yml down
