package domain.pos.payment.entity;

public enum PaymentStatus {
    /** 결제 객체 생성 후 아직 결제 수단 선택 전 초기 상태 */
    READY,
    /** 결제수단 선택 후 결제 요청까지 진행된 상태 */
    IN_PROGRESS,
    /** 가상계좌 발급 후 입금 대기 중인 상태 */
    WAITING_FOR_DEPOSIT,
    /** 결제 승인 완료 상태 */
    DONE,
    /** 결제 승인 실패 상태 */
    ABORTED,
    /** 결제 유효시간(30분) 초과로 자동 취소된 상태 */
    EXPIRED,
    /** 승인된 결제가 전액 취소된 상태 */
    CANCELED,
    /** 승인된 결제가 부분 취소된 상태 */
    PARTIAL_CANCELED
}
