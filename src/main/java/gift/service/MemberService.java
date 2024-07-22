package gift.service;

import gift.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final WishProductService wishProductService;

    public MemberService(MemberRepository memberRepository, WishProductService wishProductService) {
        this.memberRepository = memberRepository;
        this.wishProductService = wishProductService;
    }

    public void deleteMember(Long memberId) {
        wishProductService.deleteAllByMemberId(memberId);
        memberRepository.deleteById(memberId);
    }
}
