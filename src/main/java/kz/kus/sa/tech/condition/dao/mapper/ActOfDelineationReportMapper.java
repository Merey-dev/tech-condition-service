package kz.kus.sa.tech.condition.dao.mapper;

import kz.kus.commons.enums.ConsumerType;
import kz.kus.sa.registry.dto.report.v1.SignReportDto;
import kz.kus.sa.registry.dto.v1.StatementSignDto;
import kz.kus.sa.tech.condition.dao.entity.AbdAddressEntity;
import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationEntity;
import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationRenewalEntity;
import kz.kus.sa.tech.condition.dao.entity.ElectrifiedInstallationEntity;
import kz.kus.sa.tech.condition.dao.entity.embedded.RepresentativePersonEmbedded;
import kz.kus.sa.tech.condition.dto.report.act.AbdAddressReportDto;
import kz.kus.sa.tech.condition.dto.report.act.ActOfDelineationRenewalApplicationReportDto;
import kz.kus.sa.tech.condition.dto.report.act.ActOfDelineationRenewalReportDto;
import kz.kus.sa.tech.condition.dto.report.act.ElectrifiedInstallationReportDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static kz.kus.sa.tech.condition.util.CommonUtils.getFullName;

@Mapper(componentModel = "spring", uses = {
        ExternalFileMapper.class,
})
public abstract class ActOfDelineationReportMapper {

    @Mapping(target = "consumerType", source = "entity.consumerType", qualifiedByName = "consumerType")
    @Mapping(target = "representativeFullName", source = "entity.representativePerson", qualifiedByName = "representativeFullName")
    @Mapping(target = "representativePhone", source = "entity.representativePerson", qualifiedByName = "representativePhone")
    @Mapping(target = "createdDatetime", expression = "java(kz.kus.sa.tech.condition.util.CommonUtils.formattedDate(entity.getCreatedDatetime()))")
    public abstract ActOfDelineationRenewalApplicationReportDto toActOfDelineationRenewalApplicationReportDto(ActOfDelineationRenewalEntity entity);

    @Mapping(target = "preparationDatetime", expression = "java(kz.kus.sa.tech.condition.util.CommonUtils.formattedDate(entity.getPreparationDatetime()))")
    @Mapping(target = "consumerIinBin", source = "actOfDelineationRenewalExecution.actOfDelineationRenewal.consumerIinBin")
    @Mapping(target = "consumerType", source = "actOfDelineationRenewalExecution.actOfDelineationRenewal.consumerType", qualifiedByName = "consumerType")
    @Mapping(target = "consumerFullNameRu", source = "actOfDelineationRenewalExecution.actOfDelineationRenewal.consumerFullNameRu")
    @Mapping(target = "consumerFullNameKk", source = "actOfDelineationRenewalExecution.actOfDelineationRenewal.consumerFullNameKk")
    @Mapping(target = "responsibleForElectricalEquipment", source = "actOfDelineationRenewalExecution.actOfDelineationRenewal.responsibleForElectricalEquipment")
    @Mapping(target = "subdivisionNameRu", source = "actOfDelineationRenewalExecution.actOfDelineationRenewal.subdivision.ru")
    @Mapping(target = "subdivisionNameKk", source = "actOfDelineationRenewalExecution.actOfDelineationRenewal.subdivision.kk")
    public abstract ActOfDelineationRenewalReportDto toActOfDelineationRenewalReportDto(ActOfDelineationEntity entity);

    public abstract AbdAddressReportDto toAbdAddressReport(AbdAddressEntity entity);

    public abstract ElectrifiedInstallationReportDto toElectrifiedInstallationReport(ElectrifiedInstallationEntity entity);

    @Mapping(target = "signerFullName", source = "signer.fullName")
    @Mapping(target = "signerPosition", source = "signer.position")
    @Mapping(target = "signerProviderName", source = "signer.providerName")
    @Mapping(target = "signedDatetime", expression = "java(kz.kus.sa.tech.condition.util.CommonUtils.formattedDateTime(sign.getSignedDatetime()))")
    @Mapping(target = "signTypeRu", source = "sign", qualifiedByName = "signTypeRu")
    @Mapping(target = "signTypeKz", source = "sign", qualifiedByName = "signTypeKz")
    public abstract SignReportDto toReportDto(StatementSignDto sign);

    public abstract List<SignReportDto> toReportDtoList(List<StatementSignDto> signList);


    @Named("consumerType")
    protected String consumerType(ConsumerType consumerType) {
        return nonNull(consumerType) ? consumerType.name() : null;
    }

    @Named("representativeFullName")
    protected String representativeFullName(RepresentativePersonEmbedded embedded) {
        if (isNull(embedded)) return null;
        return getFullName(embedded.getFirstName(), embedded.getLastName(), embedded.getFatherName());
    }

    @Named("representativePhone")
    protected String representativePhone(RepresentativePersonEmbedded embedded) {
        if (isNull(embedded)) return null;
        return embedded.getPhone();
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
