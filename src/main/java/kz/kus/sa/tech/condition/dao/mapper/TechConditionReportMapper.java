package kz.kus.sa.tech.condition.dao.mapper;

import kz.kus.sa.auth.api.provider.ProviderApiService;
import kz.kus.sa.registry.dto.report.v1.SignReportDto;
import kz.kus.sa.registry.dto.v1.StatementSignDto;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionAbdAddressDecisionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionEntity;
import kz.kus.sa.tech.condition.dto.report.TechConditionApplicationReportDto;
import kz.kus.sa.tech.condition.dto.report.TechConditionDecisionReportDto;
import kz.kus.sa.tech.condition.dto.report.TechConditionReasonedRefusalReportDto;
import kz.kus.sa.tech.condition.dto.report.TechConditionTechRecommendationReportDto;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Slf4j
@Mapper(componentModel = "spring", uses = {
        ExternalFileMapper.class,
        ExternalUserMapper.class,
        ExternalSubdivisionMapper.class,
})
public abstract class TechConditionReportMapper {

    @Autowired
    private ProviderApiService providerApiService;

    @Mapping(target = "consumerFullNameRu", source = "techCondition.consumerFullNameRu")
    @Mapping(target = "consumerFullNameKk", source = "techCondition.consumerFullNameKk")
    @Mapping(target = "consumerIinBin", source = "techCondition.consumerIinBin")
    @Mapping(target = "isConsumerSigned", expression = "java(java.util.Objects.nonNull(entity.getTechCondition().getConsumerSignedDatetime()))")
    @Mapping(target = "applicationDatetime", expression = "java(kz.kus.sa.tech.condition.util.CommonUtils.formattedDate(entity.getTechCondition().getApplicationDatetime()))")
    @Mapping(target = "registrationNumber", source = "techCondition.statementRegistrationNumber")
    @Mapping(target = "numberOfApartments", source = "techCondition.numberOfApartments")
    @Mapping(target = "requiredPower", source = "techCondition.requiredPower")
    @Mapping(target = "completionOfConstruction", source = "techCondition.completionOfConstruction")
    @Mapping(target = "consumptionTypeCode", source = "techCondition.consumptionTypeCode")
    @Mapping(target = "electricalLoadTypeCode", source = "techCondition.electricalLoadTypeCode")
    @Mapping(target = "existingMaximumLoad", source = "techCondition.existingMaximumLoad")
    @Mapping(target = "voltageLevelCode", source = "techCondition.voltageLevelCode")
    @Mapping(target = "serviceTypeCode", source = "techCondition.serviceTypeCode")
    public abstract TechConditionApplicationReportDto toApplicationReportDto(TechConditionExecutionEntity entity);

    @Mapping(target = "consumerFullNameRu", source = "decision.techConditionExecution.techCondition.consumerFullNameRu")
    @Mapping(target = "consumerFullNameKk", source = "decision.techConditionExecution.techCondition.consumerFullNameKk")
    @Mapping(target = "consumerIinBin", source = "decision.techConditionExecution.techCondition.consumerIinBin")
    @Mapping(target = "signerFullName", source = "decision.techConditionExecution.techCondition.director.fullName")
    @Mapping(target = "signerOrganization", source = "decision.techConditionExecution.techCondition.director.providerName")
    @Mapping(target = "signerBin", source = "decision.techConditionExecution.techCondition.director.providerId", qualifiedByName = "providerBin")
    @Mapping(target = "signerPost", source = "decision.techConditionExecution.techCondition.director.position")
    @Mapping(target = "requiredPower", source = "decision.project.requiredPower")
    @Mapping(target = "registrationNumber", source = "decision.project.registrationNumber")
    @Mapping(target = "createdDatetime", expression = "java(kz.kus.sa.tech.condition.util.CommonUtils.formattedDate(decision.getProject().getCreatedDatetime()))")
    @Mapping(target = "previousRegistrationNumber", source = "decision.techConditionExecution.techCondition.previousRegistrationNumber")
    @Mapping(target = "connectionPointsRu", source = "decision.project.connectionPointsRu")
    @Mapping(target = "connectionPointsKk", source = "decision.project.connectionPointsKk")
    @Mapping(target = "connectionPointCode", source = "decision.project.connectionPointCode")
    @Mapping(target = "meteringPointCode", source = "decision.project.meteringPointCode")
    @Mapping(target = "sectionBorderRu", source = "decision.project.sectionBorderRu")
    @Mapping(target = "sectionBorderKk", source = "decision.project.sectionBorderKk")
    @Mapping(target = "allowedPowerFactor", source = "decision.project.allowedPowerFactor")
    @Mapping(target = "technicalRequirementsRu", source = "decision.project.technicalRequirementsRu")
    @Mapping(target = "technicalRequirementsKk", source = "decision.project.technicalRequirementsKk")
    @Mapping(target = "systemOperatorRequirementsRu", source = "decision.project.systemOperatorRequirementsRu")
    @Mapping(target = "systemOperatorRequirementsKk", source = "decision.project.systemOperatorRequirementsKk")
    @Mapping(target = "agreementWithOwnerRu", source = "decision.project.agreementWithOwnerRu")
    @Mapping(target = "agreementWithOwnerKk", source = "decision.project.agreementWithOwnerKk")
    @Mapping(target = "cancellationOfPrevious", source = "decision.project.cancellationOfPrevious")
    @Mapping(target = "conductivityEnhancementRu", source = "decision.project.conductivityEnhancementRu")
    @Mapping(target = "conductivityEnhancementKk", source = "decision.project.conductivityEnhancementKk")
    @Mapping(target = "techConditionValidity", source = "decision.project.techConditionValidity")
    @Mapping(target = "organizationWithAECASRu", source = "decision.project.organizationWithAECASRu")
    @Mapping(target = "organizationWithAECASKk", source = "decision.project.organizationWithAECASKk")
    @Mapping(target = "equippingWithRPDRu", source = "decision.project.equippingWithRPDRu")
    @Mapping(target = "equippingWithRPDKk", source = "decision.project.equippingWithRPDKk")
    @Mapping(target = "equippingWithSCDRu", source = "decision.project.equippingWithSCDRu")
    @Mapping(target = "equippingWithSCDKk", source = "decision.project.equippingWithSCDKk")
    @Mapping(target = "reactivePowerCompensationRu", source = "decision.project.reactivePowerCompensationRu")
    @Mapping(target = "reactivePowerCompensationKk", source = "decision.project.reactivePowerCompensationKk")
    @Mapping(target = "systemOperatorRequirementFile", source = "decision.project.systemOperatorRequirementFile")
    @Mapping(target = "serviceTypeRu", ignore = true)
    @Mapping(target = "serviceTypeKk", ignore = true)
    @Mapping(target = "voltageLevelRu", ignore = true)
    @Mapping(target = "voltageLevelKk", ignore = true)
    @Mapping(target = "electricalLoadTypeRu", ignore = true)
    @Mapping(target = "electricalLoadTypeKk", ignore = true)
    @Mapping(target = "consumptionTypeRu", ignore = true)
    @Mapping(target = "consumptionTypeKk", ignore = true)
    @Mapping(target = "reasonRu", ignore = true)
    @Mapping(target = "reasonKk", ignore = true)
    public abstract TechConditionTechRecommendationReportDto toTechnicalRecommendationReportDto(TechConditionExecutionAbdAddressDecisionEntity decision);

    @Mapping(target = "consumerFullNameRu", source = "decision.techConditionExecution.techCondition.consumerFullNameRu")
    @Mapping(target = "consumerFullNameKk", source = "decision.techConditionExecution.techCondition.consumerFullNameKk")
    @Mapping(target = "consumerIinBin", source = "decision.techConditionExecution.techCondition.consumerIinBin")
    @Mapping(target = "signerFullName", source = "decision.techConditionExecution.techCondition.director.fullName")
    @Mapping(target = "signerOrganization", source = "decision.techConditionExecution.techCondition.director.providerName")
    @Mapping(target = "signerBin", source = "decision.techConditionExecution.techCondition.director.providerId", qualifiedByName = "providerBin")
    @Mapping(target = "signerPost", source = "decision.techConditionExecution.techCondition.director.position")
    @Mapping(target = "requiredPower", source = "decision.techConditionExecution.techCondition.requiredPower")
    @Mapping(target = "registrationNumber", source = "decision.reasonForRefusalRegistrationNumber")
    @Mapping(target = "createdDatetime", expression = "java(kz.kus.sa.tech.condition.util.CommonUtils.formattedDate(decision.getReasonForRefusalDatetime()))")
    @Mapping(target = "reasonForRefusalDatetime", expression = "java(kz.kus.sa.tech.condition.util.CommonUtils.formattedDate(decision.getReasonForRefusalDatetime()))")
    @Mapping(target = "reasonForRefusalRu", source = "decision.reasonForRefusalRu")
    @Mapping(target = "reasonForRefusalKk", source = "decision.reasonForRefusalKk")
    @Mapping(target = "statementRegistrationNumber", source = "decision.techConditionExecution.techCondition.statementRegistrationNumber")
    @Mapping(target = "copy", source = "decision.techConditionExecution.techCondition.copy")
    public abstract TechConditionReasonedRefusalReportDto toTechConditionReasonedRefusalReportDto(TechConditionExecutionAbdAddressDecisionEntity decision);

    public abstract TechConditionDecisionReportDto toDecisionReportDto(TechConditionTechRecommendationReportDto dto);

    public abstract TechConditionDecisionReportDto toDecisionReportDto(TechConditionReasonedRefusalReportDto dto);

    @Mapping(target = "signerFullName", source = "signer.fullName")
    @Mapping(target = "signerPosition", source = "signer.position")
    @Mapping(target = "signerProviderName", source = "signer.providerName")
    @Mapping(target = "signedDatetime", expression = "java(kz.kus.sa.tech.condition.util.CommonUtils.formattedDateTime(sign.getSignedDatetime()))")
    @Mapping(target = "signTypeRu", source = "sign", qualifiedByName = "signTypeRu")
    @Mapping(target = "signTypeKz", source = "sign", qualifiedByName = "signTypeKz")
    public abstract SignReportDto toReportDto(StatementSignDto sign);

    public abstract List<SignReportDto> toReportDtoList(List<StatementSignDto> signList);


    @Named("providerBin")
    protected String providerBin(UUID providerId) {
        if (Objects.isNull(providerId)) return null;
        try {
            return providerApiService.getProviderDto(providerId).getIinBin();
        } catch (Exception e) {
            log.error("Provider bin not found for id=[{}]: {}", providerId, e.getMessage());
            return null;
        }
    }

    @Named("signTypeRu")
    protected String signTypeRu(StatementSignDto sign) {
        if (Objects.isNull(sign.getType())) return "";
        switch (sign.getType()) {
            case ON_PAPER: return "обычная";
            case QR:       return "QR";
            case EDS:      return "ЭЦП";
            default:       return "";
        }
    }

    @Named("signTypeKz")
    protected String signTypeKz(StatementSignDto sign) {
        if (Objects.isNull(sign.getType())) return "";
        switch (sign.getType()) {
            case ON_PAPER: return "кәдімгі";
            case QR:       return "QR";
            case EDS:      return "ЭЦҚ";
            default:       return "";
        }
    }
}
