package com.library.smartlibrary.service;

import com.library.smartlibrary.entity.Fine;
import com.library.smartlibrary.exception.BadRequestException;
import com.library.smartlibrary.exception.ResourceNotFoundException;
import com.library.smartlibrary.repository.FineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class FineService {

    @Autowired
    private FineRepository fineRepository;

    @Autowired
    private LibraryTransactionService transactionService;

    @Transactional(readOnly = true)
    public List<Fine> getAllFines() {
        return fineRepository.findAllOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public Fine getFineById(Long id) {
        return fineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Fine> getPendingFines() {
        return fineRepository.findByPaymentStatus("PENDING");
    }

    @Transactional(readOnly = true)
    public List<Fine> getFinesByMember(Long memberId) {
        return fineRepository.findByMember_MemberId(memberId);
    }

    public Fine payFine(Long fineId) {
        Fine fine = getFineById(fineId);

        if ("PAID".equalsIgnoreCase(fine.getPaymentStatus())) {
            throw new BadRequestException("Fine ID " + fineId + " has already been paid on " + fine.getPaymentDate());
        }

        fine.setPaymentStatus("PAID");
        fine.setPaymentDate(LocalDate.now());

        Fine saved = fineRepository.save(fine);

        // Record transaction
        transactionService.recordTransaction(
                "FINE_PAID",
                fine.getMember(),
                fine.getBorrow() != null ? fine.getBorrow().getBook() : null,
                "Fine payment received: ₹" + fine.getFineAmount() + " paid by " + fine.getMember().getName() + " (Fine ID: " + fine.getFineId() + ")",
                "SUCCESS"
        );

        return saved;
    }
}
