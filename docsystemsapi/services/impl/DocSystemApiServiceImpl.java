package com.bbva.enoa.platformservices.coreservice.docsystemsapi.services.impl;

import com.bbva.enoa.apirestgen.docsystemsapi.model.DocNodeDto;
import com.bbva.enoa.apirestgen.docsystemsapi.model.DocResourceDto;
import com.bbva.enoa.apirestgen.docsystemsapi.model.DocSystemDto;
import com.bbva.enoa.apirestgen.documentsmanagerapi.model.DMDocumentFolderDTO;
import com.bbva.enoa.apirestgen.qualityassuranceapi.model.QAPlanPerformanceReport;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.product.entities.DocSystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentCategory;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentType;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.IApiManagerService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DocSystemRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ScheduleRequestRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.ValidationUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IDocumentsManagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IFileTransferAdminClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IQualityAssuranceClient;
import com.bbva.enoa.platformservices.coreservice.docsystemsapi.exceptions.DocSystemErrorCode;
import com.bbva.enoa.platformservices.coreservice.docsystemsapi.services.interfaces.DocSystemApiService;
import com.bbva.enoa.platformservices.coreservice.docsystemsapi.util.Constants;
import com.bbva.enoa.platformservices.coreservice.docsystemsapi.util.DocSystemUtils;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.interfaces.ILogicalConnectorService;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class DocSystemApiServiceImpl implements DocSystemApiService
{
    private static final String CREATE_DOCSYSTEM_PERMISSION = "CREATE_DOCSYSTEM";
    private static final String DELETE_DOCSYSTEM_PERMISSION = "DELETE_DOCSYSTEM";
    private static final String UPDATE_DOCSYSTEM_PERMISSION = "UPDATE_DOCSYSTEM";

    private static final String DOC_SYSTEM_DTO_NAME_FIELD = "systemName";
    private static final String DOC_SYSTEM_DTO_URL_FIELD = "url";
    private static final String DOC_SYSTEM_DTO_CATEGORY_FIELD = "category";
    private static final String DOC_RESOURCE_DEPLOYMENT_PLAN = "Plan de Despliegue";
    private static final String DOC_RESOURCE_QUALITY_REPORT = "QUALITY_REPORT";

    private final DocSystemRepository docSystemRepo;

    private final ProductRepository productRepository;

    private final IProductUsersClient usersService;

    /**
     * Logical Connector Service.
     */
    private final ILogicalConnectorService logicalConnectorService;

    /**
     * API Manager Service.
     */
    private final IApiManagerService apiManagerService;

    /**
     * File Transfer Admin Client.
     */
    private final IFileTransferAdminClient fileTransferAdminClient;

    /**
     * Nova activities emitter
     */
    private final INovaActivityEmitter novaActivityEmitter;

    /**
     * Schedule Request repository
     */
    private final ScheduleRequestRepository scheduleRequestRepository;

    /**
     * Quality Assurance client
     */
    private final IQualityAssuranceClient qualityAssuranceClient;

    /**
     * Documents Manager client.
     */
    private final IDocumentsManagerClient documentsManagerClient;

    /**
     * @param docSystemRepo             DocSystem repository
     * @param productRepository         Product repository
     * @param usersService              User service client
     * @param logicalConnectorService   Logical Connector Service
     * @param apiManagerService         API Manager Service
     * @param fileTransferAdminClient   File Transfer Admin Client
     * @param scheduleRequestRepository Schedule Request repository.
     * @param documentsManagerClient    Documents Manager client
     * @param qualityAssuranceClient    Quality Assurance client
     * @param novaActivityEmitter       NovaActivity emitter
     */
    @Autowired
    public DocSystemApiServiceImpl(final DocSystemRepository docSystemRepo, final ProductRepository productRepository,
                                   final IProductUsersClient usersService, final ILogicalConnectorService logicalConnectorService,
                                   final IApiManagerService apiManagerService, final IFileTransferAdminClient fileTransferAdminClient,
                                   final ScheduleRequestRepository scheduleRequestRepository, final IDocumentsManagerClient documentsManagerClient,
                                   final IQualityAssuranceClient qualityAssuranceClient,
                                   final INovaActivityEmitter novaActivityEmitter)
    {
        this.docSystemRepo = docSystemRepo;
        this.productRepository = productRepository;
        this.usersService = usersService;
        this.logicalConnectorService = logicalConnectorService;
        this.apiManagerService = apiManagerService;
        this.fileTransferAdminClient = fileTransferAdminClient;
        this.scheduleRequestRepository = scheduleRequestRepository;
        this.documentsManagerClient = documentsManagerClient;
        this.qualityAssuranceClient = qualityAssuranceClient;
        this.novaActivityEmitter = novaActivityEmitter;
    }


    @Override
    public void deleteDocSystem(final String ivUser, final Integer docSystemId)
    {
        // Check if the DocSystem does exist.
        DocSystem docSystem = this.docSystemRepo.findById(docSystemId).orElseThrow(() -> new NovaException(DocSystemErrorCode.getNoSuchDocSystemError(docSystemId),
                MessageFormat.format("Doc System {0} not found when deleting doc system", docSystemId)));

        // Check permissions
        this.usersService.checkHasPermission(ivUser, DELETE_DOCSYSTEM_PERMISSION, docSystem.getProduct().getId(), new NovaException(DocSystemErrorCode.getForbiddenError()));

        // Check if it's allowed to remove this type of document
        this.checkDocSystemRemovalAllowed(docSystem);

        // Remove it from the product.
        docSystem.getProduct().getDocSystems().remove(docSystem);

        // Remove it from DB.
        this.docSystemRepo.deleteById(docSystemId);

        // Create new activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(docSystem.getProduct().getId(), ActivityScope.DOCUMENTATION, ActivityAction.ELIMINATED)
                .entityId(docSystemId)
                .addParam("type", docSystem.getProduct().getType())
                .addParam("description", docSystem.getDescription())
                .addParam("systemName", docSystem.getSystemName())
                .build());
    }

    @Override
    public DocNodeDto[] getProductDocSystems(final Integer productId)
    {
        // First of all check if product does exist in NOVA.
        // Will throw an exception if not.
        Product product = this.productRepository.findById(productId).orElseThrow(() -> new NovaException(DocSystemErrorCode.getNoSuchProductError(),
                MessageFormat.format("Product {0} not found when getting product doc systems", productId)));

        // Get list.
        List<DocSystem> docSystemList = this.docSystemRepo.findByProduct(product.getId());

        return this.buildDocSystemDtoArrayFromEntityList(docSystemList);
    }

    @Override
    public void createDocSystem(final DocSystemDto docSystemToAdd, final String ivUser, final Integer productId)
    {
        // First of all check if product does exist in NOVA.
        // Will throw an exception if not.
        Product product = this.productRepository.findById(productId).orElseThrow(() -> new NovaException(DocSystemErrorCode.getNoSuchProductError(),
                MessageFormat.format("Product {0} not found when creating doc system", productId)));

        // Check permissions
        this.usersService.checkHasPermission(ivUser, CREATE_DOCSYSTEM_PERMISSION, productId, new NovaException(DocSystemErrorCode.getForbiddenError()));

        // Get the document's category from the incoming DTO.
        DocumentCategory documentCategory;
        try
        {
            documentCategory = DocumentCategory.valueOf(docSystemToAdd.getCategory());
        }
        catch (Exception exception)
        {
            throw new NovaException(DocSystemErrorCode.getInvalidFieldError(DOC_SYSTEM_DTO_CATEGORY_FIELD, docSystemToAdd.getCategory()));
        }

        // Validate the new DocSystem.
        this.validateNewDocSystem(docSystemToAdd, documentCategory, product, Constants.DOC_SYSTEM_TYPE_WHEN_CREATED_BY_USER);

        // Create a new DocSystem and associate it to the Product.
        DocSystem docSystem = new DocSystem();
        docSystem.setUrl(docSystemToAdd.getUrl());
        docSystem.setSystemName(docSystemToAdd.getSystemName());
        docSystem.setDescription(docSystemToAdd.getDescription());
        docSystem.setProduct(product);
        docSystem.setCategory(documentCategory);
        docSystem.setType(Constants.DOC_SYSTEM_TYPE_WHEN_CREATED_BY_USER);
        docSystem.setParent(this.findParentFolder(documentCategory, product));
        this.docSystemRepo.save(docSystem);

        product.getDocSystems().add(docSystem);

        this.productRepository.save(product);

        // Create new activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(productId, ActivityScope.DOCUMENTATION, ActivityAction.ADDED)
                .entityId(docSystem.getId())
                .addParam("type", product.getType())
                .addParam("description", docSystem.getDescription())
                .addParam("systemName", docSystem.getSystemName())
                .build());
    }

    @Override
    public void updateDocSystem(DocSystemDto docSystemUpdated, Integer docSystemId, String ivUser)
    {
        // Check whether the DocSystem exists.
        DocSystem docSystem = this.docSystemRepo.findById(docSystemId).orElseThrow(() -> new NovaException(DocSystemErrorCode.getNoSuchDocSystemError(docSystemId)));

        // Check permissions
        this.usersService.checkHasPermission(ivUser, UPDATE_DOCSYSTEM_PERMISSION, docSystem.getProduct().getId(), new NovaException(DocSystemErrorCode.getForbiddenError(), DocSystemErrorCode.getForbiddenError().toString()));

        // Get the document's category from the incoming DTO.
        DocumentCategory documentCategory;
        try
        {
            documentCategory = DocumentCategory.valueOf(docSystemUpdated.getCategory());
        }
        catch (Exception exception)
        {
            throw new NovaException(DocSystemErrorCode.getInvalidFieldError(DOC_SYSTEM_DTO_CATEGORY_FIELD, docSystemUpdated.getCategory()));
        }
        // Validate the updated DocSystem.
        this.validateUpdatedDocSystem(docSystemUpdated, documentCategory, docSystem.getProduct(), docSystem, Constants.DOC_SYSTEM_TYPE_WHEN_CREATED_BY_USER);

        // Update DocSystem, but only the allowed fields.
        docSystem.setUrl(docSystemUpdated.getUrl());
        docSystem.setSystemName(docSystemUpdated.getSystemName());
        docSystem.setDescription(docSystemUpdated.getDescription());
        docSystem.setCategory(documentCategory);
        docSystem.setParent(this.findParentFolder(documentCategory, docSystem.getProduct()));
        this.docSystemRepo.save(docSystem);

        // Create new activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(docSystem.getProduct().getId(), ActivityScope.DOCUMENTATION, ActivityAction.MODIFIED)
                .entityId(docSystem.getId())
                .addParam("type", docSystem.getProduct().getType())
                .addParam("description", docSystem.getDescription())
                .addParam("systemName", docSystem.getSystemName())
                .build());
    }

    @Override
    public DocNodeDto getDocSystem(Integer docSystemId)
    {
        // Find the Doc System entity.
        DocSystem docSystem = this.docSystemRepo.findById(docSystemId).orElseThrow(() -> new NovaException(DocSystemErrorCode.getNoSuchDocSystemError(docSystemId)));

        // Find a list with the rest of Doc Systems in the same product, in which to find its children.
        List<DocSystem> docSystemInTheSameProductList = this.docSystemRepo.findByProduct(docSystem.getProduct().getId());

        // Build and return a hierarchy for the Doc System entity.
        return this.buildDocNodeDto(docSystem, docSystemInTheSameProductList);
    }

    @Override
    @Transactional
    @Deprecated
    // Does not work anymore, because the column "documents_link" has been removed from the entity ScheduleRequest.
    public void createDocSystemsHierarchy(String ivUser)
    {
        /*final String className = this.getClass().getSimpleName();
        final String methodName = "createDocSystemsHierarchy";

        if (this.usersService.isPlatformAdmin(ivUser))
        {
            // Get all the products and iterate through them.
            List<Product> productList = this.productRepository.findAllByOrderByUuaaAsc();
            log.info("[{}] -> [{}]: creating doc systems hierarchy for [{}] products", className, methodName, productList.size());
            Integer counter = 0;
            for (Product product : productList)
            {
                counter++;
                try
                {
                    log.info("[{}] -> [{}]: creating doc system hierarchy for product [{} - {}] (#{} of {})", className, methodName, product.getId(), product.getUuaa(), counter, productList.size());
                    // 1. If the root folder already exists, but not its children folder, create them. If the root folder does not exist, an exception will be thrown.
                    DocSystem rootFolderDocumentEntity = this.findRootFolder(product);
                    if (rootFolderDocumentEntity != null)
                    {
                        log.info("[{}] -> [{}]: root folder for product [{} - {}] is [{}]", className, methodName, product.getId(), product.getUuaa(), rootFolderDocumentEntity.getId());
                        // 1.1. Get which children folder don't already exist.
                        List<DocumentCategory> expectedCategories = Arrays.stream(DocumentCategory.values()).filter(documentCategory -> !documentCategory.getIsRootCategory()).collect(Collectors.toList());
                        List<DocumentCategory> missingCategories = new ArrayList<>();
                        for (DocumentCategory expectedCategory : expectedCategories)
                        {
                            if (!this.existsParentFolder(expectedCategory, product))
                            {
                                missingCategories.add(expectedCategory);
                            }

                        }
                        // 2. Call Documents Manager Client to create the subfolders (currently, in Google Drive).
                        if (missingCategories.size() > 0)
                        {
                            log.info("[{}] -> [{}]: missing categories for product [{} - {}]] are [{}]", className, methodName, product.getId(), product.getUuaa(), missingCategories.stream().map(DocumentCategory::getName).collect(Collectors.joining(", ")));
                            DMDocumentDTO rootFolderDocumentDTO = this.documentsManagerClient.createDocumentFoldersForProduct(product, missingCategories.stream().map(DocumentCategory::getName).collect(Collectors.toList()));
                            log.info("[{}] -> [{}]: response from documents manager client for product [{}] is [{}]", className, methodName, product.getId(), rootFolderDocumentDTO);
                            // 3. Build the children folders of root document, save them and associate them with a product.
                            if (rootFolderDocumentDTO != null)
                            {
                                List<DocSystem> childrenFolderEntities = this.docSystemService.buildAndSaveChildrenFolders(product, rootFolderDocumentDTO, rootFolderDocumentEntity);
                                log.info("[{}] -> [{}]: children entities for product [{} - {}] are [{}]", className, methodName, product.getId(), product.getUuaa(), childrenFolderEntities.stream().map(docSystem -> String.valueOf(docSystem.getId())).collect(Collectors.joining(", ")));
                            }
                            else
                            {
                                throw new NovaException(DocSystemErrorCode.getUnexpectedError(String.format("Response when creating document folders for product [%d] is null", product.getId())));
                            }
                        }

                        // 4. Migrate the DocSystems that already existed before calling this endpoint.
                        List<DocSystem> docSystemToMigrateList = this.docSystemRepo.findByProductIdAndAutogeneratedByPlatform(product.getId(), false);
                        for (DocSystem docSystemToMigrate : docSystemToMigrateList)
                        {
                            // 4.1. Set the parent of the DocSystem to its corresponding folder.
                            if (docSystemToMigrate.getParent() == null)
                            {
                                log.info("[{}] -> [{}]: migrating DocSystem [{}] from product [{} - {}]", className, methodName, docSystemToMigrate.getId(), product.getId(), product.getUuaa());
                                DocSystem parentFolderOfDocSystemToMigrate = this.findParentFolder(docSystemToMigrate.getCategory(), product);
                                docSystemToMigrate.setParent(parentFolderOfDocSystemToMigrate);
                                log.info("[{}] -> [{}]: set parent of DocSystem [{}] (from product [{} - {}]) to [{}]", className, methodName, docSystemToMigrate.getId(), product.getId(), product.getUuaa(), parentFolderOfDocSystemToMigrate.getId());
                            }
                            else
                            {
                                log.warn("[{}] -> [{}]: DocSystem [{}] in product [{} - {}] already migrated", className, methodName, docSystemToMigrate.getId(), product.getId(), product.getUuaa());
                            }
                        }

                        // 5. Migrate documentation of Schedule Requests.
                        List<ScheduleRequest> scheduleRequestsArray = this.scheduleRequestRepository.findByProductIdAndStatusNotIn(
                                product.getId(), List.of(ScheduleReqStatus.REJECTED, ScheduleReqStatus.UNSCHEDULE_REQUEST_SENT, ScheduleReqStatus.UNSCHEDULED, ScheduleReqStatus.DELETED));
                        log.info("[{}] -> [{}]: found [{}] Schedule Requests for product [{} - {}]", className, methodName, scheduleRequestsArray.size(), product.getId(), product.getUuaa());
                        for (ScheduleRequest scheduleRequest : scheduleRequestsArray)
                        {
                            String scheduleRequestDocumentLink = scheduleRequest.getDocumentsLink();
                            // 5.1. Migration only makes sense for Schedule Requests with a "document_link".
                            if (!Strings.isEmpty(scheduleRequestDocumentLink))
                            {
                                log.info("[{}] -> [{}]: migrating documentation for Schedule Request [{}] in product [{} - {}]", className, methodName, scheduleRequest.getId(), product.getId(), product.getUuaa());
                                DocumentCategory categoryForScheduleRequestDocuments = DocumentCategory.BATCH_SCHEDULE;
                                DocumentType typeForNewDocSystemForScheduleRequest = DocumentType.FILE;
                                // 5.2. Make sure that the documentation of this Schedule Request is not already migrated.
                                String nameForNewDocSystemForScheduleRequest = String.format("%s - %d - %s", scheduleRequest.getEnvironment(), scheduleRequest.getId(), DocSystemUtils.getFileDocSystemName(categoryForScheduleRequestDocuments.getName(), product));
                                if (this.docSystemRepo.findBySystemNameAndProductIdAndCategoryAndType(nameForNewDocSystemForScheduleRequest, product.getId(), categoryForScheduleRequestDocuments, typeForNewDocSystemForScheduleRequest).isEmpty())
                                {
                                    // 5.3. Create the new DocSystem.
                                    DocSystem newDocSystemForScheduleRequest = new DocSystem(
                                            nameForNewDocSystemForScheduleRequest,
                                            DocSystemUtils.getFileDocSystemDescription(categoryForScheduleRequestDocuments.getName(), product),
                                            scheduleRequestDocumentLink,
                                            product,
                                            categoryForScheduleRequestDocuments,
                                            typeForNewDocSystemForScheduleRequest,
                                            this.findParentFolder(categoryForScheduleRequestDocuments, product),
                                            false
                                    );
                                    // 5.4. Save the new DocSystem and associate it with the Schedule Request.
                                    this.docSystemRepo.save(newDocSystemForScheduleRequest);
                                    scheduleRequest.setBatchScheduleDocument(newDocSystemForScheduleRequest);
                                    log.info("[{}] -> [{}]: set document [{}] to Schedule Request [{}] in product [{} - {}]", className, methodName, newDocSystemForScheduleRequest.getId(), scheduleRequest.getId(), product.getId(), product.getUuaa());
                                }
                                else
                                {
                                    log.info("[{}] -> [{}]: documentation for Schedule Request [{}] in product [{} - {}] was already migrated", className, methodName, scheduleRequest.getId(), product.getId(), product.getUuaa());
                                }
                            }
                            else
                            {
                                log.info("[{}] -> [{}]: no document_link for Schedule Request [{}] in product [{} - {}]", className, methodName, scheduleRequest.getId(), product.getId(), product.getUuaa());
                            }
                        }
                    }
                }
                catch (NovaException e)
                {
                    log.error("[{}] -> [{}]: NOVA exception in product [{} - {}]: {}", className, methodName, product.getId(), product.getUuaa(), e.getNovaError().getErrorMessage());
                }
                catch (Exception e)
                {
                    log.error("[{}] -> [{}]: exception in product [{} - {}]: {}", className, methodName, product.getId(), product.getUuaa(), e);
                }
            }
            log.info("[{}] -> [{}]: created doc systems hierarchy for [{}] products", className, methodName, productList.size());
        }
        else
        {
            log.error("[{}] -> [{}]: {}", className, methodName, "Only admins are allowed to call this endpoint");
            throw new NovaException(DocSystemErrorCode.getForbiddenError());
        }*/
    }

    @Override
    public void createDocSystemProductsFolder(final String folderCategory)
    {
        // Verify that the name of the folder to create is registered as a valid category
        DocumentCategory documentCategory = Arrays.stream(DocumentCategory.values())
                .filter(dc -> dc.toString().equalsIgnoreCase(folderCategory.trim()))
                .findAny().orElseThrow(() -> new NovaException(DocSystemErrorCode.getNoSuchDocSystemError(folderCategory)));
        List<String> folders = new ArrayList<>();
        folders.add(documentCategory.getName());

        // Get all the products to include the folder in its documentation structure
        List<Product> products = productRepository.findAll();

        for (Product product : products)
        {
            try
            {
                DocSystem rootFolder = this.findRootFolder(product);
                DMDocumentFolderDTO[] documentFolders = documentsManagerClient.createDocumentFoldersForProduct(product, folders).getFolders();
                for (DMDocumentFolderDTO documentFolder : documentFolders)
                {
                    // If the folder was already created in drive, it is not registered again in the DDBB
                    if (this.docSystemRepo.findBySystemNameAndProductIdAndCategoryAndType(DocSystemUtils.getChildFolderDocSystemName(documentFolder.getName(), product),
                            product.getId(), documentCategory, DocumentType.FOLDER).isEmpty())
                    {
                        DocSystem childFolderDocSystem = new DocSystem(
                                DocSystemUtils.getChildFolderDocSystemName(documentFolder.getName(), product),
                                DocSystemUtils.getChildFolderDocSystemDescription(documentFolder.getName(), product),
                                documentFolder.getUrl(),
                                product,
                                documentCategory,
                                DocumentType.FOLDER,
                                rootFolder,
                                true
                        );
                        docSystemRepo.saveAndFlush(childFolderDocSystem);
                    }
                }
            }
            catch (Exception e)
            {
                log.warn("Problems creating new folder. The product [{}] does not have a correct documentation system to be able to create a new folder", product.getUuaa());
            }
        }
    }

    /**
     * Find the DocSystem representing the folder for a given category in a product, e.g. the "MSA" folder of a product.
     *
     * @param documentCategory The given category.
     * @param product          The given product.
     * @return The DocSystem representing the folder for a given category in a product, e.g. the "MSA" folder of a product.
     */
    private DocSystem findParentFolder(DocumentCategory documentCategory, Product product)
    {
        final String methodName = "findParentFolder";

        List<DocSystem> docSystemList = this.docSystemRepo.findByProduct(product.getId()).stream()
                .filter(docSystem -> docSystem.getCategory() == documentCategory && docSystem.getType() == DocumentType.FOLDER)
                .collect(Collectors.toList());
        if (docSystemList.size() != 1)
        {
            this.logAndThrowUnexpectedError(methodName, String.format("Found [%d] folders for category [%s] in product [%d - %s]", docSystemList.size(), documentCategory, product.getId(), product.getUuaa()));
        }
        return docSystemList.get(0);
    }

    /**
     * Find the root folder of a product. Currently, it's expected just one, if there are more or less, an exception will be thrown.
     *
     * @param product The given product.
     * @return The root folder, if any.
     */
    private DocSystem findRootFolder(Product product)
    {
        final String methodName = "findRootFolder";

        // Check that there is only one root category defined in the model.
        List<DocumentCategory> rootCategoriesList = Arrays.stream(DocumentCategory.values()).filter(DocumentCategory::getIsRootCategory).collect(Collectors.toList());
        if (rootCategoriesList.size() == 0)
        {
            this.logAndThrowUnexpectedError(methodName, "No root document categories defined in model");

        }
        else if (rootCategoriesList.size() > 1)
        {
            this.logAndThrowUnexpectedError(methodName, String.format("[%d] root categories defined in model: [%s]", rootCategoriesList.size(), rootCategoriesList.stream().map(DocumentCategory::getName).collect(Collectors.joining(", "))));
        }
        // If there is only one root category defined in the model, get it and find the root folder of this category for the given product.
        DocumentCategory rootCategory = rootCategoriesList.get(0);
        return this.findParentFolder(rootCategory, product);
    }

    /**
     * Log an error and throw an "Unexpected error" exception.
     *
     * @param methodName   Name of the method that generated or captured the error. For logging purposes.
     * @param errorMessage Error message to log and to throw in the exception.
     */
    private void logAndThrowUnexpectedError(String methodName, String errorMessage)
    {
        log.error("[{}] -> [{}]: {}", this.getClass().getSimpleName(), methodName, errorMessage);
        throw new NovaException(DocSystemErrorCode.getUnexpectedError(errorMessage));
    }

    /**
     * Whether exists a DocSystem representing the folder for a given category in a product, e.g. the "MSA" folder of a product.
     *
     * @param documentCategory The given category.
     * @param product          The given product.
     * @return Whether exists.
     */
    private Boolean existsParentFolder(DocumentCategory documentCategory, Product product)
    {
        return this.docSystemRepo.findByProduct(product.getId()).stream()
                .anyMatch(docSystem -> docSystem.getCategory() == documentCategory && docSystem.getType() == DocumentType.FOLDER);
    }

    /**
     * Builds an array of DocSystemDto from a list of DocSystem entities. The later is an unordered flat list, with files and folders all at the same level,
     * and the former is a structure that relates parents and children in a tree-like structure, more suitable to be represented as a true hierarchy of files and folders.
     *
     * @param documentEntityList A list of DocSystem entities.
     * @return An array of DocNodeDto.
     */
    private DocNodeDto[] buildDocSystemDtoArrayFromEntityList(List<DocSystem> documentEntityList)
    {
        // A result list where the document nodes will be stored.
        List<DocNodeDto> documentNodeResultList = new ArrayList<>();

        // For each root document entity in the entity list, i.e., each document entity that has no parent: build it along with its children:
        documentEntityList.stream()
                .filter(documentEntity -> documentEntity.getParent() == null)
                .sorted(Comparator.comparing(rootDocumentEntity -> rootDocumentEntity.getCategory().ordinal()))
                .forEach(rootDocumentEntity -> documentNodeResultList.add(this.buildDocNodeDto(rootDocumentEntity, documentEntityList)));

        return documentNodeResultList.toArray(DocNodeDto[]::new);
    }

    /**
     * Build and return a new DocNodeDto from a DocSystem entity.
     *
     * @param docSystem     The given DocSystem entity.
     * @param docSystemList A list of DocSystem entities where to find its children.
     * @return A new DocNodeDto.
     */
    private DocNodeDto buildDocNodeDto(DocSystem docSystem, List<DocSystem> docSystemList)
    {
        DocNodeDto docNodeDto = new DocNodeDto();
        if (docSystem != null)
        {
            docNodeDto.setId(docSystem.getId());
            docNodeDto.setNodeType(String.valueOf(docSystem.getType()));
            docNodeDto.setCategory(String.valueOf(docSystem.getCategory()));
            docNodeDto.setSystemName(docSystem.getSystemName());
            docNodeDto.setNodeDisplayName(this.generateNodeDisplayName(docSystem));
            docNodeDto.setDescription(docSystem.getDescription());
            docNodeDto.setUrl(docSystem.getUrl());
            docNodeDto.setResources(this.buildDocResourceDtoArray(docSystem));
            docNodeDto.setAssociated(docSystem.getAssociated());

            // Find the children in the list of entities.
            List<DocSystem> childrenAsDocSystemList = docSystemList.stream()
                    .filter(documentEntity -> documentEntity.getParent() == docSystem)
                    .sorted(Comparator.comparing(documentEntity -> documentEntity.getCategory().ordinal()))
                    .collect(Collectors.toList());

            // Build recursively each child and add it to a partial list of DTO children.
            DocNodeDto[] childrenAsDocNodeDtoArray = new DocNodeDto[childrenAsDocSystemList.size()];
            int i = 0;
            for (DocSystem child : childrenAsDocSystemList)
            {
                childrenAsDocNodeDtoArray[i] = this.buildDocNodeDto(child, docSystemList);
                i++;
            }
            // Set the children from the list of DTO children.
            docNodeDto.setChildren(childrenAsDocNodeDtoArray);
        }

        return docNodeDto;
    }

    /**
     * Generate the name that will be actually shown in clients of the current API.
     *
     * @param docSystem A document.
     * @return The name to be shown.
     */
    private String generateNodeDisplayName(DocSystem docSystem)
    {
        if (DocumentType.FOLDER.equals(docSystem.getType()))
        {
            if (DocumentCategory.MSA.equals(docSystem.getCategory()))
            {
                return "Documentación de Solutions Architect";
            }
            if (DocumentCategory.ARA.equals(docSystem.getCategory()))
            {
                return "Documentación de Seguridad";
            }
            if (DocumentCategory.BATCH_SCHEDULE.equals(docSystem.getCategory()))
            {
                return "Documentación de planificación en Control M";
            }
            if (DocumentCategory.PERFORMANCE_REPORTS.equals(docSystem.getCategory()))
            {
                return "Documentación de Informes de Rendimiento";
            }
            if (DocumentCategory.OTHER.equals(docSystem.getCategory()))
            {
                return "Otra documentación";
            }
        }
        return docSystem.getSystemName();
    }

    /**
     * Build and return a new DocResourceDto array from a DocSystem entity, fetching the related "resource entities" (logical connectors, APIs, etc.) from database.
     *
     * @param docSystem The given DocSystem entity.
     * @return A new DocResourceDto array
     */
    private DocResourceDto[] buildDocResourceDtoArray(DocSystem docSystem)
    {
        List<DocResourceDto> docResourceDtoList = new ArrayList<>();
        if (docSystem != null)
        {
            // Depending of the category of document, it can have different types of resources associated.
            // MSA has Logical Connectors, File Transfer Configs and NOVA APIs.
            if (DocumentCategory.MSA.equals(docSystem.getCategory()))
            {
                // Fetch, build and add Logical Connectors resources.
                this.logicalConnectorService.getLogicalConnectorUsingMsaDocument(docSystem.getId()).forEach(logicalConnector ->
                        docResourceDtoList.add(this.buildDocResourceDto(logicalConnector.getId(), logicalConnector.getName(), Constants.RESOURCE_LOGICAL_CONNECTOR, logicalConnector.getEnvironment() != null ? logicalConnector.getEnvironment() : null)));
                // Fetch, build and add File Transfer Configs resources.
                Arrays.stream(this.fileTransferAdminClient.findFileTransferConfigByProductId(docSystem.getProduct().getId()))
                        .filter(fileTransferConfig -> docSystem.getId().equals(fileTransferConfig.getMsaDocumentId())).forEach(fileTransferConfig ->
                                docResourceDtoList.add(this.buildDocResourceDto(fileTransferConfig.getId(), fileTransferConfig.getName(), Constants.RESOURCE_FILE_TRANSFER_CONFIG, fileTransferConfig.getEnvironment()))
                        );
                // Fetch, build and add NOVA APIs resources.
                this.apiManagerService.getApisUsingMsaDocument(docSystem.getId()).forEach(syncApi ->
                        docResourceDtoList.add(this.buildDocResourceDto(syncApi.getId(), syncApi.getName(), Constants.RESOURCE_NOVA_API, null)));
            }
            // ARA has Logical Connectors, File Transfer Configs and NOVA APIs.
            else if (DocumentCategory.ARA.equals(docSystem.getCategory()))
            {
                // Fetch, build and add Logical Connectors resources.
                this.logicalConnectorService.getLogicalConnectorUsingAraDocument(docSystem.getId()).forEach(logicalConnector ->
                        docResourceDtoList.add(this.buildDocResourceDto(logicalConnector.getId(), logicalConnector.getName(), Constants.RESOURCE_LOGICAL_CONNECTOR, logicalConnector.getEnvironment() != null ? logicalConnector.getEnvironment() : null)));
                // Fetch, build and add File Transfer Configs resources.
                Arrays.stream(this.fileTransferAdminClient.findFileTransferConfigByProductId(docSystem.getProduct().getId()))
                        .filter(fileTransferConfig -> docSystem.getId().equals(fileTransferConfig.getAraDocumentId())).forEach(fileTransferConfig ->
                                docResourceDtoList.add(this.buildDocResourceDto(fileTransferConfig.getId(), fileTransferConfig.getName(), Constants.RESOURCE_FILE_TRANSFER_CONFIG, fileTransferConfig.getEnvironment()))
                        );
                // Fetch, build and add NOVA APIs resources.
                this.apiManagerService.getApisUsingAraDocument(docSystem.getId()).forEach(syncApi ->
                        docResourceDtoList.add(this.buildDocResourceDto(syncApi.getId(), syncApi.getName(), Constants.RESOURCE_NOVA_API, null)));

            }
            // "Batch schedule" documents have Schedule Requests.
            else if (DocumentCategory.BATCH_SCHEDULE.equals(docSystem.getCategory()))
            {
                this.scheduleRequestRepository.findByBatchScheduleDocumentId(docSystem.getId()).forEach(scheduleRequest ->
                        docResourceDtoList.add(this.buildDocResourceDto(Math.toIntExact(scheduleRequest.getId()), scheduleRequest.getDescription(), Constants.RESOURCE_SCHEDULE_REQUEST, scheduleRequest.getEnvironment() != null ? scheduleRequest.getEnvironment() : null)));
            }
            // "PERFORMANCE_REPORTS" documents have associated plan.
            else if (DocumentCategory.PERFORMANCE_REPORTS.equals(docSystem.getCategory()))
            {
                // Fetch, build and add Performance report resource.
                QAPlanPerformanceReport planPerformanceReport = this.qualityAssuranceClient.getPerformanceReportsByLink(docSystem.getUrl());
                if (planPerformanceReport != null)
                {
                    docResourceDtoList.add(this.buildDocResourceDto(planPerformanceReport.getPlanId(), DOC_RESOURCE_DEPLOYMENT_PLAN, DOC_RESOURCE_QUALITY_REPORT, Environment.PRE.getEnvironment()));
                }
            }
            // Do nothing for documents of category "Google Drive" or "Other", and throw an exception for not handled categories.
            else if (!DocumentCategory.GOOGLE_DRIVE.equals(docSystem.getCategory()) && !DocumentCategory.OTHER.equals(docSystem.getCategory()))
            {
                throw new NovaException(DocSystemErrorCode.getUnexpectedError(String.format("Category [%s] not handled yet while building a DTO for a document entity", docSystem.getCategory())));
            }
        }
        return docResourceDtoList.toArray(new DocResourceDto[0]);
    }

    /**
     * Build and return a new DocResourceDto from the given fields of a resource related to a document.
     *
     * @param resourceId   The ID of the resource.
     * @param resourceName The name of the resource.
     * @param resourceType The type of the resource.
     * @param environment  The environment of the resource, if any.
     * @return A new DocResourceDto.
     */
    private DocResourceDto buildDocResourceDto(Integer resourceId, String resourceName, String resourceType, String environment)
    {
        DocResourceDto docResourceDto = new DocResourceDto();
        docResourceDto.setId(resourceId);
        docResourceDto.setName(resourceName);
        docResourceDto.setType(resourceType);
        docResourceDto.setEnvironment(environment);
        return docResourceDto;
    }

    /**
     * Validates that a new DocSystem has correct values, and it doesn't collide with an already existing DocSystem (i.e. same name, product, category and type).
     *
     * @param docSystemDto     The DTO that represents the new DocSystem.
     * @param documentCategory The category inferred from the DTO.
     * @param documentType     The type of the document being updated.
     * @param product          The Product.
     */
    private void validateNewDocSystem(DocSystemDto docSystemDto, DocumentCategory documentCategory, Product product, DocumentType documentType)
    {
        this.validateDocSystemDtoFields(docSystemDto, documentCategory, product);

        // If there is already a DocSystem with the same name, product, category and type, throw an exception.
        if (this.docSystemRepo.findBySystemNameAndProductIdAndCategoryAndType(docSystemDto.getSystemName(), product.getId(), documentCategory, documentType).isPresent())
        {
            throw new NovaException(DocSystemErrorCode.getRepeatedDocSystemError(docSystemDto.getSystemName(), docSystemDto.getCategory(), product.getId()));
        }
        // If there is already a DocSystem file with the same category and url, throw an exception for PERFORMANCE_REPORTS category
        if (documentCategory == DocumentCategory.PERFORMANCE_REPORTS)
        {
            if (this.docSystemRepo.findByUrlAndCategoryAndType(docSystemDto.getUrl(), DocumentCategory.PERFORMANCE_REPORTS, DocumentType.FILE).isPresent())
            {
                throw new NovaException(DocSystemErrorCode.getRepeatedFileDocSystemError(docSystemDto.getUrl(), docSystemDto.getCategory()));
            }
        }
    }

    /**
     * Validates that updating a DocSystem uses correct values, and doesn't collide with other already existing DocSystem (i.e. same name, product, category and type).
     *
     * @param docSystemDto     The DTO that represents the DocSystem being updated.
     * @param documentCategory The category inferred from the DTO.
     * @param documentType     The type of the document being updated.
     * @param product          The Product.
     * @param docSystem        The DocSystem being updated
     */
    private void validateUpdatedDocSystem(DocSystemDto docSystemDto, DocumentCategory documentCategory, Product product, DocSystem docSystem, DocumentType documentType)
    {
        this.validateDocSystemDtoFields(docSystemDto, documentCategory, product);

        // Find a DocSystem with the same name, product, category and type.
        this.docSystemRepo.findBySystemNameAndProductIdAndCategoryAndType(docSystemDto.getSystemName(), product.getId(), documentCategory, documentType).ifPresent(existingDocSystem -> {
            // If the DocSystem found is the one that we're updating: fine, no collision, so do nothing. If not: it's a collision, so throw an exception.
            if (!existingDocSystem.getId().equals(docSystem.getId()))
            {
                throw new NovaException(DocSystemErrorCode.getRepeatedDocSystemError(docSystemDto.getSystemName(), docSystemDto.getCategory(), product.getId()));
            }
        });

        this.checkDocSystemUpdateAllowed(docSystemDto, docSystem);
    }

    /**
     * Validates that the DocSystem has correct values.
     *
     * @param docSystemDto     The incoming DTO.
     * @param documentCategory The category inferred from the DTO.
     * @param product          The name of the Product.
     */
    private void validateDocSystemDtoFields(DocSystemDto docSystemDto, DocumentCategory documentCategory, Product product)
    {
        // Verify required fields.
        ValidationUtils.verifyNotNull(docSystemDto.getSystemName(), new NovaException(DocSystemErrorCode.getInvalidFieldError(DOC_SYSTEM_DTO_NAME_FIELD, docSystemDto.getSystemName())));
        ValidationUtils.verifyNotNull(docSystemDto.getUrl(), new NovaException(DocSystemErrorCode.getInvalidFieldError(DOC_SYSTEM_DTO_URL_FIELD, docSystemDto.getUrl())));

        // It's not allowed to create or update DocSystems of some categories.
        if (!this.isDocSystemCategoryAllowedWhenEndUserCreatesOrUpdates(documentCategory))
        {
            throw new NovaException(DocSystemErrorCode.getDocSystemCategoryCreationOrUpdateNotAllowedForEndUsersError(docSystemDto.getCategory()));
        }

        // Verify that the name doesn't collide with the name of the root folder of the product.
        if (docSystemDto.getSystemName().toLowerCase().equals(DocSystemUtils.getDriveDocSystemName(product)))
        {
            throw new NovaException(DocSystemErrorCode.getDocSystemNameIsReservedError(docSystemDto.getSystemName()));
        }
    }

    /**
     * Check whether a DocSystem is allowed to be removed.
     *
     * @param docSystem The DocSystem.
     * @throws NovaException Thrown if the given DocSystem isn't allowed be removed.
     */
    private void checkDocSystemRemovalAllowed(DocSystem docSystem)
    {
        if (docSystem.getAutogeneratedByPlatform())
        {
            throw new NovaException(DocSystemErrorCode.getDocSystemRemovalNotAllowedError(docSystem.getId(), "Was autogenerated by the platform, and cannot be removed manually."));
        }
        else if (this.buildDocResourceDtoArray(docSystem).length > 0)
        {
            throw new NovaException(DocSystemErrorCode.getDocSystemRemovalNotAllowedError(docSystem.getId(), "Has resources associated."));
        }
    }

    /**
     * Check whether a DocSystem is allowed to be updated.
     *
     * @param docSystemDto    The DocSystem DTO.
     * @param docSystemEntity The DocSystem Entity.
     * @throws NovaException Thrown if the given DocSystem isn't allowed be updated.
     */
    private void checkDocSystemUpdateAllowed(DocSystemDto docSystemDto, DocSystem docSystemEntity)
    {
        if (docSystemEntity.getAutogeneratedByPlatform())
        {
            throw new NovaException(DocSystemErrorCode.getDocSystemUpdateNotAllowedError(docSystemEntity.getId(), "Was autogenerated by the platform, and cannot be updated manually."));
        }
        else
        {
            String docSystemDtoCategory = docSystemDto.getCategory();
            String docSystemEntityCategory = docSystemEntity.getCategory() != null ? docSystemEntity.getCategory().name() : null;
            // Don't allow to update the category when it has resources associated
            if (!docSystemDtoCategory.equals(docSystemEntityCategory) && this.buildDocResourceDtoArray(docSystemEntity).length > 0)
            {
                throw new NovaException(DocSystemErrorCode.getDocSystemUpdateNotAllowedError(docSystemEntity.getId(), "The category cannot be updated when it has resources associated."));
            }
        }
    }

    /**
     * Whether end-users are allowed to create or update a document belonging to the given category.
     * Currently, all categories are allowed except the root.
     *
     * @param category The given category.
     * @return Whether end-users are allowed to create or update a document belonging to the given category.
     */
    private boolean isDocSystemCategoryAllowedWhenEndUserCreatesOrUpdates(DocumentCategory category)
    {
        return !category.getIsRootCategory();
    }

}
