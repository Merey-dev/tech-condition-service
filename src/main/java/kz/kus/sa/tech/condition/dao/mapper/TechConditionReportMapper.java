package kz.kus.sa.tech.condition.dao.mapper;

import kz.kus.sa.auth.api.provider.ProviderApiService;
import kz.kus.sa.registry.dto.report.v1.SignReportDto;
import kz.kus.sa.registry.dto.v1.StatementSignDto;
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

    @Mapping(target = "consumerFullNameRu", source = "project.consumerFullNameRu")
    @Mapping(target = "consumerFullNameKk", source = "project.consumerFullNameKk")
    @Mapping(target = "consumerIinBin", source = "project.consumerIinBin")
    @Mapping(target = "signerFullName", source = "techCondition.director.fullName")
    @Mapping(target = "signerOrganization", source = "techCondition.director.providerName")
    @Mapping(target = "signerBin", source = "techCondition.director.providerId", qualifiedByName = "providerBin")
    @Mapping(target = "signerPost", source = "techCondition.director.position")
    @Mapping(target = "requiredPower", source = "project.requiredPower")
    @Mapping(target = "registrationNumber", source = "project.registrationNumber")
    @Mapping(target = "createdDatetime", expression = "java(kz.kus.sa.tech.condition.util.CommonUtils.formattedDate(entity.getProject().getCreatedDatetime()))")
    @Mapping(target = "previousRegistrationNumber", source = "techCondition.previousRegistrationNumber")
    @Mapping(target = "connectionPointsRu", source = "project.connectionPointsRu")
    @Mapping(target = "connectionPointsKk", source = "project.connectionPointsKk")
    @Mapping(target = "connectionPointCode", source = "project.connectionPointCode")
    @Mapping(target = "meteringPointCode", source = "project.meteringPointCode")
    @Mapping(target = "serviceTypeRu", ignore = true)
    @Mapping(target = "serviceTypeKk", ignore = true)
    @Mapping(target = "voltageLevelRu", ignore = true)
    @Mapping(target = "voltageLevelKk", ignore = true)
    @Mapping(target = "electricalLoadTypeRu", ignore = true)
    @Mapping(target = "electricalLoadTypeKk", ignore = true)
    @Mapping(target = "consumptionTypeRu", ignore = true)
    @Mapping(target = "consumptionTypeKk", ignore = true)
    @Mapping(target = "sectionBorderRu", source = "project.sectionBorderRu")
    @Mapping(target = "sectionBorderKk", source = "project.sectionBorderKk")
    @Mapping(target = "allowedPowerFactor", source = "project.allowedPowerFactor")
    @Mapping(target = "technicalRequirementsRu", source = "project.technicalRequirementsRu")
    @Mapping(target = "technicalRequirementsKk", source = "project.technicalRequirementsKk")
    @Mapping(target = "systemOperatorRequirementsRu", source = "project.systemOperatorRequirementsRu")
    @Mapping(target = "systemOperatorRequirementsKk", source = "project.systemOperatorRequirementsKk")
    @Mapping(target = "agreementWithOwnerRu", source = "project.agreementWithOwnerRu")
    @Mapping(target = "agreementWithOwnerKk", source = "project.agreementWithOwnerKk")
    @Mapping(target = "cancellationOfPrevious", source = "project.cancellationOfPrevious")
    @Mapping(target = "conductivityEnhancementRu", source = "project.conductivityEnhancementRu")
    @Mapping(target = "conductivityEnhancementKk", source = "project.conductivityEnhancementKk")
    @Mapping(target = "reasonRu", ignore = true)
    @Mapping(target = "reasonKk", ignore = true)
    @Mapping(target = "techConditionValidity", source = "project.techConditionValidity")
    @Mapping(target = "organizationWithAECASRu", source = "project.organizationWithAECASRu")
    @Mapping(target = "organizationWithAECASKk", source = "project.organizationWithAECASKk")
    @Mapping(target = "equippingWithRPDRu", source = "project.equippingWithRPDRu")
    @Mapping(target = "equippingWithRPDKk", source = "project.equippingWithRPDKk")
    @Mapping(target = "equippingWithSCDRu", source = "project.equippingWithSCDRu")
    @Mapping(target = "equippingWithSCDKk", source = "project.equippingWithSCDKk")
    @Mapping(target = "reactivePowerCompensationRu", source = "project.reactivePowerCompensationRu")
    @Mapping(target = "reactivePowerCompensationKk", source = "project.reactivePowerCompensationKk")
    @Mapping(target = "systemOperatorRequirementFile", source = "project.systemOperatorRequirementFile")
    public abstract TechConditionTechRecommendationReportDto toTechnicalRecommendationReportDto(TechConditionExecutionEntity entity);

    public abstract TechConditionDecisionReportDto toDecisionReportDto(TechConditionTechRecommendationReportDto dto);

    @Mapping(target = "consumerFullNameRu", source = "techCondition.consumerFullNameRu")
    @Mapping(target = "consumerFullNameKk", source = "techCondition.consumerFullNameKk")
    @Mapping(target = "consumerIinBin", source = "techCondition.consumerIinBin")
    @Mapping(target = "signerFullName", source = "techCondition.director.fullName")
    @Mapping(target = "signerOrganization", source = "techCondition.director.providerName")
    @Mapping(target = "signerBin", source = "techCondition.director.providerId", qualifiedByName = "providerBin")
    @Mapping(target = "signerPost", source = "techCondition.director.position")
    @Mapping(target = "requiredPower", source = "techCondition.requiredPower")
    @Mapping(target = "registrationNumber", source = "reasonForRefusalRegistrationNumber")
    @Mapping(target = "createdDatetime", expression = "java(kz.kus.sa.tech.condition.util.CommonUtils.formattedDate(entity.getReasonForRefusalDatetime()))")
    @Mapping(target = "reasonForRefusalDatetime", expression = "java(kz.kus.sa.tech.condition.util.CommonUtils.formattedDate(entity.getReasonForRefusalDatetime()))")
    @Mapping(target = "statementRegistrationNumber", source = "techCondition.statementRegistrationNumber")
    @Mapping(target = "copy", source = "techCondition.copy")
    public abstract TechConditionReasonedRefusalReportDto toTechConditionReasonedRefusalReportDto(TechConditionExecutionEntity entity);

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
        return providerApiService.getProviderDto(providerId).getIinBin();
    }

    @Named("signTypeRu")
    protected String signTypeRu(StatementSignDto sign) {
        if (Objects.isNull(sign.getType()))
            return "";
        switch (sign.getType()) {
            case ON_PAPER:
                return "обычная";
            case QR:
                return "QR";
            case EDS:
                return "ЭЦП";
            default:
                return "";
        }
    }

    @Named("signTypeKz")
    protected String signTypeKz(StatementSignDto sign) {
        if (Objects.isNull(sign.getType()))
            return "";
        switch (sign.getType()) {
            case ON_PAPER:
                return "кәдімгі";
            case QR:
                return "QR";
            case EDS:
                return "ЭЦҚ";
            default:
                return "";
        }
    }
}
