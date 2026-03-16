#!/usr/bin/env bash
# =============================================================================
# MySQL Master-Replica 레플리케이션 초기화 스크립트
# docker-compose-replication.yml 기동 후 최초 1회 실행
# =============================================================================

set -e

MASTER_HOST="localhost"
MASTER_PORT="3315"
REPLICA_PORT="3316"
ROOT_PASS="1234"
REPL_USER="replicator"
REPL_PASS="repl1234"

echo "[1/5] Master에 레플리케이션 유저 생성 및 권한 부여..."
mysql -h ${MASTER_HOST} -P ${MASTER_PORT} -uroot -p${ROOT_PASS} <<SQL
CREATE USER IF NOT EXISTS '${REPL_USER}'@'%' IDENTIFIED WITH mysql_native_password BY '${REPL_PASS}';
GRANT REPLICATION SLAVE ON *.* TO '${REPL_USER}'@'%';
FLUSH PRIVILEGES;
SQL

echo "[2/5] Master binlog 상태 조회..."
MASTER_STATUS=$(mysql -h ${MASTER_HOST} -P ${MASTER_PORT} -uroot -p${ROOT_PASS} -e "SHOW MASTER STATUS\G" 2>/dev/null)
BINLOG_FILE=$(echo "${MASTER_STATUS}" | grep "File:" | awk '{print $2}')
BINLOG_POS=$(echo "${MASTER_STATUS}" | grep "Position:" | awk '{print $2}')

echo "  -> File: ${BINLOG_FILE}, Position: ${BINLOG_POS}"

echo "[3/5] Replica에 Master 정보 설정..."
mysql -h ${MASTER_HOST} -P ${REPLICA_PORT} -uroot -p${ROOT_PASS} <<SQL
STOP REPLICA;
CHANGE REPLICATION SOURCE TO
  SOURCE_HOST='mysql-master',
  SOURCE_PORT=3306,
  SOURCE_USER='${REPL_USER}',
  SOURCE_PASSWORD='${REPL_PASS}',
  SOURCE_LOG_FILE='${BINLOG_FILE}',
  SOURCE_LOG_POS=${BINLOG_POS};
START REPLICA;
SQL

echo "[4/5] Replica 상태 확인 (10초 대기)..."
sleep 10
mysql -h ${MASTER_HOST} -P ${REPLICA_PORT} -uroot -p${ROOT_PASS} -e "SHOW REPLICA STATUS\G" 2>/dev/null \
  | grep -E "Replica_IO_Running|Replica_SQL_Running|Seconds_Behind_Source"

echo "[5/5] 레플리케이션 초기화 완료."
echo ""
echo "테스트 데이터 삽입 (Master에만 실행):"
echo "  docker exec -i yabam-mysql-master mysql -uroot -p1234 local_mydb < ./scripts/full_init_data_v3.sql"
