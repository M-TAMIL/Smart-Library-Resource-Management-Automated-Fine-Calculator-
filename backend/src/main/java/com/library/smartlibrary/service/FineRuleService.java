package com.library.smartlibrary.service;

import com.library.smartlibrary.entity.FineRule;
import com.library.smartlibrary.exception.ResourceNotFoundException;
import com.library.smartlibrary.repository.FineRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FineRuleService {

    @Autowired
    private FineRuleRepository fineRuleRepository;

    @Transactional(readOnly = true)
    public List<FineRule> getAllRules() {
        return fineRuleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public FineRule getActiveRule() {
        return fineRuleRepository.findFirstByStatusOrderByRuleIdDesc("ACTIVE")
                .orElseGet(() -> {
                    FineRule defaultRule = new FineRule(5.0, 0, 100.0, "ALL", "ACTIVE");
                    return fineRuleRepository.save(defaultRule);
                });
    }

    public FineRule addRule(FineRule rule) {
        if ("ACTIVE".equalsIgnoreCase(rule.getStatus())) {
            // Deactivate other rules to maintain one primary active rule
            List<FineRule> existing = fineRuleRepository.findByStatus("ACTIVE");
            for (FineRule r : existing) {
                r.setStatus("INACTIVE");
            }
            fineRuleRepository.saveAll(existing);
        }
        return fineRuleRepository.save(rule);
    }

    public FineRule updateRule(Long id, FineRule updated) {
        FineRule existing = fineRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fine rule not found with id: " + id));

        if (updated.getFinePerDay() != null) existing.setFinePerDay(updated.getFinePerDay());
        if (updated.getGracePeriod() != null) existing.setGracePeriod(updated.getGracePeriod());
        if (updated.getMaximumFine() != null) existing.setMaximumFine(updated.getMaximumFine());
        if (updated.getMembershipType() != null) existing.setMembershipType(updated.getMembershipType());
        if (updated.getStatus() != null) {
            if ("ACTIVE".equalsIgnoreCase(updated.getStatus()) && !"ACTIVE".equalsIgnoreCase(existing.getStatus())) {
                List<FineRule> others = fineRuleRepository.findByStatus("ACTIVE");
                for (FineRule r : others) {
                    if (!r.getRuleId().equals(id)) {
                        r.setStatus("INACTIVE");
                    }
                }
                fineRuleRepository.saveAll(others);
            }
            existing.setStatus(updated.getStatus());
        }

        return fineRuleRepository.save(existing);
    }
}
