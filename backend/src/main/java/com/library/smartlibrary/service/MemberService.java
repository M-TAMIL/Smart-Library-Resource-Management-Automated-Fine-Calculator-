package com.library.smartlibrary.service;

import com.library.smartlibrary.entity.Borrow;
import com.library.smartlibrary.entity.Member;
import com.library.smartlibrary.exception.BadRequestException;
import com.library.smartlibrary.exception.ResourceNotFoundException;
import com.library.smartlibrary.repository.BorrowRepository;
import com.library.smartlibrary.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private LibraryTransactionService transactionService;

    @Transactional(readOnly = true)
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
    }

    public Member addMember(Member member) {
        if (member.getEmail() == null || member.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Member email is required");
        }
        if (memberRepository.findByEmail(member.getEmail().trim()).isPresent()) {
            throw new BadRequestException("Member with email " + member.getEmail() + " already exists");
        }
        if (member.getStatus() == null || member.getStatus().trim().isEmpty()) {
            member.setStatus("ACTIVE");
        }

        Member saved = memberRepository.save(member);
        transactionService.recordTransaction(
                "MEMBER_REGISTERED",
                saved,
                null,
                "New member registered: " + saved.getName() + " (" + saved.getDepartment() + " - " + saved.getMembershipType() + ")",
                "SUCCESS"
        );
        return saved;
    }

    public Member updateMember(Long id, Member updated) {
        Member existing = getMemberById(id);

        if (updated.getEmail() != null && !updated.getEmail().trim().equalsIgnoreCase(existing.getEmail())) {
            memberRepository.findByEmail(updated.getEmail().trim()).ifPresent(m -> {
                if (!m.getMemberId().equals(id)) {
                    throw new BadRequestException("Another member with email " + updated.getEmail() + " already exists");
                }
            });
            existing.setEmail(updated.getEmail().trim());
        }

        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getPhone() != null) existing.setPhone(updated.getPhone());
        if (updated.getDepartment() != null) existing.setDepartment(updated.getDepartment());
        if (updated.getMembershipType() != null) existing.setMembershipType(updated.getMembershipType());
        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());

        return memberRepository.save(existing);
    }

    public void deleteMember(Long id) {
        Member member = getMemberById(id);

        List<Borrow> activeBorrows = borrowRepository.findByMember_MemberId(id);
        boolean hasActive = activeBorrows.stream().anyMatch(b -> "BORROWED".equals(b.getStatus()));
        if (hasActive) {
            throw new BadRequestException("Cannot delete member " + member.getName() + " because they have active borrowed books");
        }

        transactionService.recordTransaction(
                "MEMBER_DELETED",
                null,
                null,
                "Member deleted: " + member.getName() + " (" + member.getEmail() + ")",
                "SUCCESS"
        );
        memberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public List<Member> searchMembers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllMembers();
        }
        return memberRepository.searchMembers(query.trim());
    }
}
