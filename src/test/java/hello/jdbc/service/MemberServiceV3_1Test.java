package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 트랜잭션 - 트랜잭션 매니저
 */
@Slf4j
class MemberServiceV3_1Test {
    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_C = "ex";

    private MemberRepositoryV3 memberRepositoryV3;
    private MemberServiceV3_1 memberServiceV3;
    @BeforeEach
    void before(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        memberRepositoryV3 = new MemberRepositoryV3(dataSource);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        memberServiceV3 = new MemberServiceV3_1(transactionManager, memberRepositoryV3);
    }

    @AfterEach
    void after() throws SQLException {
        memberRepositoryV3.delete(MEMBER_A);
        memberRepositoryV3.delete(MEMBER_B);
        memberRepositoryV3.delete(MEMBER_C);
    }
    
    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepositoryV3.save(memberA);
        memberRepositoryV3.save(memberB);
        //when
        log.info("START TX");
        memberServiceV3.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
        log.info("END TX");
        //then
        Member findMemberA = memberRepositoryV3.findById(memberA.getMemberId());
        Member findMemberB = memberRepositoryV3.findById(memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체중 예외 발생")
    void accountTransferEx() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberC = new Member(MEMBER_C, 10000);
        memberRepositoryV3.save(memberA);
        memberRepositoryV3.save(memberC);
        //when
        assertThatThrownBy(() -> memberServiceV3.accountTransfer(memberA.getMemberId(), memberC.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);
        //then
        Member findMemberA = memberRepositoryV3.findById(memberA.getMemberId());
        Member findMemberB = memberRepositoryV3.findById(memberC.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(10000);
    }
}