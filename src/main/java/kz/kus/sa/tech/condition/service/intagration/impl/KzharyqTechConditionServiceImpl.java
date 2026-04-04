package kz.kus.sa.tech.condition.service.intagration.impl;

import kz.kus.commons.enums.ConsumerType;
import kz.kus.commons.utils.BASE64DecodedMultipartFile;
import kz.kus.sa.auth.api.provider.ProviderApiService;
import kz.kus.sa.auth.api.provider.dto.ProviderDto;
import kz.kus.sa.consumer.api.ConsumerApiService;
import kz.kus.sa.document.api.DocumentApiService;
import kz.kus.sa.file.api.FileApiService;
import kz.kus.sa.file.api.FileSharedApiService;
import kz.kus.sa.integ.kzharyq.dto.common.ObjectAddressDto;
import kz.kus.sa.integ.kzharyq.dto.common.SubConsumerDto;
import kz.kus.sa.integ.kzharyq.dto.common.TechConditionStatus;
import kz.kus.sa.integ.kzharyq.dto.internal.tc.FileDto;
import kz.kus.sa.integ.kzharyq.dto.internal.tc.ReliabilityCategoryDto;
import kz.kus.sa.integ.kzharyq.dto.internal.tc.completed.TechConditionCompletedRequestDto;
import kz.kus.sa.integ.kzharyq.dto.internal.tc.registered.TechConditionRegisteredRequestDto;
import kz.kus.sa.registry.api.RegistryApiService;
import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionEntity;
import kz.kus.sa.tech.condition.dao.mapper.KzharyqTechConditionServiceMapper;
import kz.kus.sa.tech.condition.dao.repository.TechConditionExecutionRepository;
import kz.kus.sa.tech.condition.dao.repository.TechConditionRepository;
import kz.kus.sa.tech.condition.enums.ExecutionStatus;
import kz.kus.sa.tech.condition.exception.BadRequestException;
import kz.kus.sa.tech.condition.service.address.AbdAddressService;
import kz.kus.sa.tech.condition.service.intagration.KzharyqTechConditionService;
import kz.kus.sa.tech.condition.service.kafka.KafkaProducer;
import kz.kus.sa.tech.condition.service.tech.condition.TechConditionReliabilityCategoryService;
import kz.kus.sa.tech.condition.service.tech.condition.TechConditionSubConsumerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static kz.kus.sa.registry.enums.Status.COMPLETED;
import static kz.kus.sa.registry.enums.TechConditionExecutionDecisionType.REASONED_REFUSAL;
import static kz.kus.sa.registry.enums.TechConditionExecutionDecisionType.TECHNICAL_RECOMMENDATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class KzharyqTechConditionServiceImpl implements KzharyqTechConditionService {

    @Value("${tech-condition.kafka.topics.tech-condition.kzharyq-tc-registered-in}")
    private String kzharyqTcRegisteredIn;
    @Value("${tech-condition.kafka.topics.tech-condition.kzharyq-tc-complete-in}")
    private String kzharyqTcCompleteIn;

    private final static String KZH_BIN = "021140000722";

    private final KafkaProducer kafkaProducer;
    private final FileApiService fileApiService;
    private final AbdAddressService abdAddressService;
    private final ProviderApiService providerApiService;
    private final RegistryApiService registryApiService;
    private final ConsumerApiService consumerApiService;
    private final DocumentApiService documentApiService;
    private final FileSharedApiService fileSharedApiService;
    private final TechConditionRepository techConditionRepository;
    private final TechConditionSubConsumerService techConditionSubConsumerService;
    private final TechConditionExecutionRepository techConditionExecutionRepository;
    private final KzharyqTechConditionServiceMapper kzharyqTechConditionServiceMapper;
    private final TechConditionReliabilityCategoryService techConditionReliabilityCategoryService;

    @Override
    public TechConditionRegisteredRequestDto getRegisteredRequest(TechConditionEntity tc) {
        var out = kzharyqTechConditionServiceMapper.toDto(tc);

        if (tc.getConsumerType().equals(ConsumerType.ORGANIZATION)) {
            var statement = registryApiService.getByStatementId(tc.getStatementId());
            var full = Stream.of(statement.getRepresentativePerson().getLastName(),
                            statement.getRepresentativePerson().getFirstName(),
                            statement.getRepresentativePerson().getFatherName())
                    .filter(e -> nonNull(e) && e.isBlank())
                    .collect(Collectors.joining(" "));
            out.setRepresentativeName(full.isEmpty() ? null : full);
        }
        var consumer = consumerApiService.getConsumer(
                tc.getConsumerIinBin(),
                KZH_BIN,
                ConsumerType.valueOf(tc.getConsumerType().name()));
        out.setPhones(Collections.singletonList(consumer.getPhone()));
        out.setEmail(consumer.getEmail());

        abdAddressService.getByTechConditionId(tc.getId()).stream()
                .findFirst()//todo list
                .ifPresent(e -> out.setAddress(ObjectAddressDto.builder()
                        .locationRu(e.getLocationRu())
                        .locationKk(e.getLocationKk())
                        .nameRu(e.getEndUseRu())
                        .nameKk(e.getEndUseRu())
                        .cadastreNumber(e.getCadastralNumber())
                        .arRcaCode(e.getArRcaCode())
                        .build()));

        out.setFiles(tc.getDocumentsWithTypeCodes().stream()
                .map(e -> {
                    var document = documentApiService.getById(tc.getConsumerIinBin(), e.getDocument());
                    var externalId = UUID.fromString(document.getExternalId());
                    var fileInfo = fileApiService.getById(externalId);
                    var uuid = UUID.randomUUID();
                    var mockMultipartFile = new BASE64DecodedMultipartFile(
                            fileInfo.getOriginName(),
                            fileInfo.getOriginName(),
                            fileInfo.getContentType(),
                            fileApiService.download(externalId));
                    fileSharedApiService.uploadFilesAsMultipart(List.of(mockMultipartFile));

                    return FileDto.builder()
                            .fileName(uuid + this.getExtension(fileInfo.getOriginName()))
                            .typeCode(e.getDocumentTypeCode())
                            .build();
                }).collect(Collectors.toList()));

        out.setSubConsumers(techConditionSubConsumerService.getByTechConditionId(tc.getId()).stream()
                .map(e -> SubConsumerDto.builder()
                        .fullName(e.getFullName())
                        .powerConsumption(e.getPowerConsumption())
                        .build())
                .collect(Collectors.toList()));
        out.setReliabilityCategories(techConditionReliabilityCategoryService.getByTechConditionId(tc.getId()).stream()
                .map(e -> ReliabilityCategoryDto.builder()
                        .reliabilityCategoryTypeCode(e.getReliabilityCategoryTypeCode())
                        .kw(e.getKwt())
                        .build())
                .collect(Collectors.toList()));
        return out;
    }

    @Override
    public void sendRegisteredRequest(TechConditionEntity tc) {
        ProviderDto provider = providerApiService.getProviderDto(tc.getProviderId());
        if (provider.getIinBin().equals(KZH_BIN)) {
            try {
                var out = this.getRegisteredRequest(tc);

                kafkaProducer.sendMessage(kzharyqTcRegisteredIn, out);

                tc.getKzharyqData().setKzharyqSentRegistered(true);
                techConditionRepository.save(tc);

                log.info("SENDING TECH CONDITION TO 1C KZHARYQ [REGISTERED]: id = [{}], registrationNumber = [{}]",
                        tc.getId(), tc.getStatementRegistrationNumber());
            } catch (Exception e) {
                log.error("ERROR SENDING TECH CONDITION TO 1C KZHARYQ [REGISTERED]: id = [{}], registrationNumber = [{}], error = [{}]",
                        tc.getId(), tc.getStatementRegistrationNumber(), e.getMessage(), e);
            }
        }
    }

    @Override
    public TechConditionCompletedRequestDto getCompletedRequest(TechConditionEntity tc, TechConditionExecutionEntity execution) {
        var out = TechConditionCompletedRequestDto.builder();
//        var project = execution.getProject();
//
//        if (Objects.equals(execution.getDecisionType(), TECHNICAL_RECOMMENDATION)) {
//            kzharyqTechConditionServiceMapper.map(out, project);
//            out.status(TechConditionStatus.valueOf(execution.getDecisionType().name()))
//                    .techConditionRegistrationDate(project.getCreatedDatetime().toLocalDateTime())
//                    .reliabilityCategories(techConditionReliabilityCategoryService.getByTechConditionId(tc.getId()).stream()
//                            .map(e -> ReliabilityCategoryDto.builder()
//                                    .reliabilityCategoryTypeCode(e.getReliabilityCategoryTypeCode())
//                                    .kw(e.getKwt())
//                                    .build())
//                            .collect(Collectors.toList()))
//                    .hasSubConsumers(tc.getHasSubConsumers())
//                    .subConsumers(techConditionSubConsumerService.getByTechConditionId(tc.getId()).stream()
//                            .map(e -> SubConsumerDto.builder()
//                                    .fullName(e.getFullName())
//                                    .powerConsumption(e.getPowerConsumption())
//                                    .build())
//                            .collect(Collectors.toList()))
//                    .installedTransformer(execution.getInstalledTransformer())
//                    .maximumTransformerLoad(execution.getMaximumTransformerLoad())
//                    .existsPlaceInstallMeteringDevice(execution.getExistsPlaceInstallMeteringDevice())
//                    .connectionPointVoltage(execution.getConnectionPointVoltage())
//                    .connectionPointVoltageLevel(execution.getConnectionPointVoltageLevel())
//                    .requiredForConnection(execution.getRequiredForConnection())
//                    .requirementsForOrganizationElectricityMetering(execution.getRequirementsForOrganizationElectricityMetering());
//        } else if (Objects.equals(execution.getDecisionType(), REASONED_REFUSAL)) {
//            out.status(TechConditionStatus.valueOf(execution.getDecisionType().name()))
//                    .refusalReasonCode(execution.getRefusalReasonCode())
//                    .techConditionRegistrationDate(execution.getReasonForRefusalDatetime().toLocalDateTime())
//                    .techConditionRegistrationNumber(execution.getReasonForRefusalRegistrationNumber())
//                    .reasonForRefusalRu(execution.getReasonForRefusalRu())
//                    .reasonForRefusalKk(execution.getReasonForRefusalKk());
//        } else {
//            if (Objects.equals(execution.getStatusCode(), ExecutionStatus.REFUSED_BY_CONSUMER.getCode())) {
//                out.techConditionRegistrationDate(tc.getLastModifiedDatetime().toLocalDateTime())
//                        .status(TechConditionStatus.REFUSED_BY_CONSUMER);
//            } else {
//                log.error("ERROR STATUS FOR SENDING TECH CONDITION TO 1C KZHARYQ [COMPLETED] [{}, {}, {}]",
//                        tc.getId(), tc.getStatusCode(), execution.getStatusCode());
//                throw new BadRequestException("ERROR STATUS FOR SENDING TECH CONDITION TO 1C KZHARYQ [COMPLETED]");
//            }
//        }
//        if (nonNull(execution.getDecisionType())) {
//            out.files(tc.getDocumentsWithTypeCodes().stream()
//                    .map(e -> {
//                        var document = documentApiService.getById(tc.getConsumerIinBin(), e.getDocument());
//                        var fileInfo = fileApiService.getById(UUID.fromString(document.getExternalId()));
//                        return FileDto.builder()
//                                .fileName(fileInfo.getOriginName())
//                                .typeCode(e.getDocumentTypeCode())
//                                .build();
//                    }).collect(Collectors.toList()));
//        }
        return out.build();
    }

    @Override
    public void sendCompletedRequest(TechConditionEntity tc) {
        ProviderDto provider = providerApiService.getProviderDto(tc.getProviderId());
        if (provider.getIinBin().equals(KZH_BIN)) {
            if (tc.getStatusCode().equals(COMPLETED.getCode())) {
                try {
                    List<TechConditionExecutionEntity> executionList = techConditionExecutionRepository
                            .findAllByTechConditionIdAndDeletedDatetimeIsNullOrderByCreatedDatetime(tc.getId());

                    for (TechConditionExecutionEntity execution : executionList) {
                        var out = this.getCompletedRequest(tc, execution);

                        kafkaProducer.sendMessage(kzharyqTcCompleteIn, out);
                    }

                    tc.getKzharyqData().setKzharyqSentCompleted(true);
                    techConditionRepository.save(tc);

                    log.info("SENDING TECH CONDITION TO 1C KZHARYQ [COMPLETED]: id = [{}], registrationNumber = [{}]",
                            tc.getId(), tc.getStatementRegistrationNumber());
                } catch (Exception e) {
                    log.error("ERROR SENDING TECH CONDITION TO 1C KZHARYQ [COMPLETED]: id = [{}], registrationNumber = [{}], error = [{}]",
                            tc.getId(), tc.getStatementRegistrationNumber(), e.getMessage(), e);
                }
            } else {
                log.error("ERROR STATUS FOR SENDING TECH CONDITION TO 1C KZHARYQ [COMPLETED] [{}, {}]",
                        tc.getId(), tc.getStatusCode());
            }
        }
    }

    private String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf('.'));
    }
}
