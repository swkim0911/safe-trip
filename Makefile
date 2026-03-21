SHELL := /bin/bash
.PHONY: dev infra stop

# 전체 기동: docker + BE + FE 병렬 실행 (Ctrl+C로 BE+FE 종료)
dev:
	docker compose -f docker-compose.dev.yml up -d
	@trap 'kill %1 %2 2>/dev/null; exit 0' INT; \
	(cd BE && ./gradlew bootRun) & \
	(cd FE && npm run dev) & \
	wait

# docker 인프라만 기동 (BE는 IntelliJ, FE는 VSCode에서 따로 실행할 때)
infra:
	docker compose -f docker-compose.dev.yml up -d

# 전체 종료 (BE + FE 프로세스 + docker)
stop:
	-pkill -f 'bootRun'
	-pkill -f 'vite'
	docker compose -f docker-compose.dev.yml down
