package kz.kus.sa.tech.condition.service.act;

import kz.kus.sa.auth.api.provider.dto.SubdivisionDto;
import kz.kus.sa.registry.dto.common.AssignDto;
import kz.kus.sa.registry.dto.common.SignCreateDto;
import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationRenewalAbdAddressDecisionEntity;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationRenewalAbdAddressDecisionDto;

import java.util.List;
import java.util.UUID;

public interface ActOfDelineationRenewalAbdAddressDecisionService {

    void assign(UUID renewalId, AssignDto dto);

    void bulkAssignAllAddresses(UUID renewalId, List<UUID> assignees, SubdivisionDto subdivisionForAll);

    ActOfDelineationRenewalAbdAddressDecisionEntity findById(UUID decisionId);

    List<ActOfDelineationRenewalAbdAddressDecisionEntity> findAllByRenewalId(UUID renewalId);

    List<ActOfDelineationRenewalAbdAddressDecisionDto> getAllByRenewalId(UUID renewalId);

    ActOfDelineationRenewalAbdAddressDecisionDto getById(UUID decisionId);

    void takeToExecution(UUID decisionId);

    void sendForApproval(UUID decisionId, AssignDto dto);

    void sendForRevision(UUID decisionId, String reason);

    void sendForConfirmation(UUID decisionId, AssignDto dto);

    void providerSign(UUID decisionId, SignCreateDto sign);

    void consumerSign(UUID decisionId, SignCreateDto sign);
}
