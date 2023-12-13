package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

class MemberRepositoryTest {
    MemberRepository repository = new MemberRepository();
    @Test
    void crud() throws SQLException {
        Member member = new Member("member", 10000);
        repository.save(member);
    }

}