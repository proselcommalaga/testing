package com.bbva.enoa.platformservices.coreservice.docsystemsapi.services.impl;

import com.bbva.enoa.apirestgen.docsystemsapi.model.DocSystemDto;
import com.bbva.enoa.apirestgen.documentsmanagerapi.model.DMDocumentDTO;
import com.bbva.enoa.apirestgen.documentsmanagerapi.model.DMDocumentFolderDTO;
import com.bbva.enoa.datamodel.model.product.entities.DocSystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentCategory;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DocSystemRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IDocumentsManagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.docsystemsapi.util.DocSystemUtils;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DocSystemApiServiceImplTest
{

    @Mock
    private DocSystemRepository docSystemRepo;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private IProductUsersClient usersService;
    @Mock
    private INovaActivityEmitter novaActivityEmitter;
    @Mock
    private IDocumentsManagerClient documentsManagerClient;
    @Mock
    private DocSystem docSystem;
    @Mock
    private Product product;
    @Mock
    List<DocSystem> dockSystemsList;

    @InjectMocks
    private DocSystemApiServiceImpl docSystemApiService;


    @BeforeEach
    public void setUp() throws Exception
    {

        MockitoAnnotations.initMocks(this);
    }

    @Test
    void deleteDocSystem()
    {

        // Generating mocks
        String ivUser = "E000000";
        this.docSystem = this.generateDocSystemMock();

        // Generating mock in case we invoke method to find
        when(this.docSystemRepo.findById(any())).thenReturn(Optional.of(docSystem));
        when(this.dockSystemsList.remove(any(DocSystem.class))).thenReturn(true);

        doNothing().when(this.usersService).checkHasPermission(anyString(), anyString(), anyInt(), any(NovaException.class));
        doNothing().when(this.novaActivityEmitter).emitNewActivity(any(GenericActivity.class));

        this.docSystemApiService.deleteDocSystem(ivUser, this.docSystem.getId());

        verify(this.docSystemRepo, times(1)).findById(this.docSystem.getId());
        verify(this.docSystemRepo, times(1)).deleteById(this.docSystem.getId());
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));


    }

    @Test
    void deleteDocSystemNoDocSystem()
    {

        // Generating mocks
        String ivUser = "E000000";
        this.docSystem = null;

        Assertions.assertThrows(NovaException.class, () -> this.docSystemApiService.deleteDocSystem(ivUser, null));
        verify(this.docSystemRepo, times(1)).findById(null);

    }

    @Test
    void deleteDocSystemNoPermission()
    {

        // Generating mocks
        String ivUser = "E000000";
        this.docSystem = this.generateDocSystemMock();

        // Generating mock in case we invoke method to generate error
        when(this.docSystemRepo.findById(any())).thenReturn(Optional.of(docSystem));
        doThrow(NovaException.class).when(this.usersService).checkHasPermission(anyString(), anyString(), anyInt(), any(NovaException.class));

        Assertions.assertThrows(NovaException.class, () -> this.docSystemApiService.deleteDocSystem("E000000", this.docSystem.getId()));
        verify(this.usersService, times(1)).checkHasPermission(anyString(), anyString(), anyInt(), any(NovaException.class));

    }


    @Test
    void deleteDocSystemCantDeleteNull()
    {

        // Generating mocks
        String ivUser = "E000000";
        this.docSystem = this.generateDocSystemMock();

        // Generating mock in case we invoke method to generate error
        when(this.docSystemRepo.findById(any())).thenReturn(null);

        // Assertions
        Assertions.assertThrows(NullPointerException.class, () -> this.docSystemApiService.deleteDocSystem("E000000", null));

    }


    @Test
    void getProductDocSystems()
    {

        // Generating mocks
        String ivUser = "E000000";
        this.product = this.generateProductMock();
        this.docSystem = this.generateDocSystemMock();

        List<DocSystem> docSystemsListMock = new ArrayList<>();
        docSystemsListMock.add(this.docSystem);

        //Expected result
        DocSystemDto[] dtoListResult = new DocSystemDto[1];

        DocSystemDto dto = new DocSystemDto();
        dto.setDescription(this.docSystem.getDescription());
        dto.setUrl(this.docSystem.getUrl());
        dto.setSystemName(this.docSystem.getSystemName());
        dto.setId(this.docSystem.getId());

        dtoListResult[0] = dto;


        // BBDD calls return mocked objects
        when(this.productRepository.findById(anyInt())).thenReturn(Optional.of(this.product));
        when(this.docSystemRepo.findByProduct(anyInt())).thenReturn(docSystemsListMock);

        // Call method
        this.docSystemApiService.getProductDocSystems(this.product.getId());

        // Assertions
        Assertions.assertSame(dtoListResult[0].getId(), this.docSystem.getId());
        Assertions.assertSame(dtoListResult[0].getDescription(), this.docSystem.getDescription());
        Assertions.assertSame(dtoListResult[0].getSystemName(), this.docSystem.getSystemName());
        Assertions.assertSame(dtoListResult[0].getUrl(), this.docSystem.getUrl());

    }

    @Test
    void getProductDocSystemsNoProduct()
    {

        // Generating mocks
        String ivUser = "E000000";

        // Assertions
        Assertions.assertThrows(NovaException.class, () -> this.docSystemApiService.getProductDocSystems(null));

    }

    @Test
    void createDocSystem()
    {

        // Generating mocks
        String ivUser = "E000000";
        this.docSystem = this.generateDocSystemMock();
        this.product = this.generateProductMock();
        DocSystemDto docSystemDto = this.generateDocSystemDtoMock();

        DocumentCategory documentCategory = DocumentCategory.valueOf(docSystemDto.getCategory());

        DocSystem parentDocument = new DocSystem();
        parentDocument.setCategory(documentCategory);
        parentDocument.setType(DocumentType.FOLDER);

        when(this.productRepository.findById(anyInt())).thenReturn(Optional.of(this.product));
        when(this.docSystemRepo.findBySystemNameAndProductIdAndCategoryAndType(anyString(), anyInt(), any(), any())).thenReturn(Optional.empty());
        when(this.docSystemRepo.findByProduct(anyInt())).thenReturn(List.of(parentDocument));
        doNothing().when(this.usersService).checkHasPermission(anyString(), anyString(), anyInt(), any(NovaException.class));
        when(this.productRepository.save(this.product)).thenReturn(this.product);

        this.docSystemApiService.createDocSystem(docSystemDto, ivUser, this.product.getId());

        verify(this.docSystemRepo, times(1)).findBySystemNameAndProductIdAndCategoryAndType(
                docSystemDto.getSystemName(), this.product.getId(), documentCategory, DocumentType.FILE);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
        verify(this.productRepository, times(1)).save(this.product);

    }


    @Test
    void createDocSystemNoProduct()
    {

        // Generating mocks
        String ivUser = "E000000";
        this.docSystem = this.generateDocSystemMock();
        this.product = this.generateProductMock();

        Assertions.assertThrows(NovaException.class, () -> this.docSystemApiService.createDocSystem(this.generateDocSystemDtoMock(), ivUser, null));

    }

    @Test
    void createDocSystemNoDocSystem()
    {

        // Generating mocks
        String ivUser = "E000000";
        this.docSystem = this.generateDocSystemMock();
        this.product = this.generateProductMock();
        DocSystemDto docSystemDto = this.generateDocSystemDtoMock();


        when(this.productRepository.findById(anyInt())).thenReturn(Optional.of(this.product));
        doNothing().when(this.usersService).checkHasPermission(anyString(), anyString(), anyInt(), any(NovaException.class));
        when(this.docSystemRepo.findBySystemNameAndProductIdAndCategoryAndType(
                docSystemDto.getSystemName(), this.product.getId(), DocumentCategory.ARA, DocumentType.FILE)).thenReturn(Optional.of(this.docSystem));

        Assertions.assertThrows(NovaException.class, () -> this.docSystemApiService.createDocSystem(docSystemDto, ivUser, this.product.getId()));

    }

    @Test
    void createDocSystemProductsFolder()
    {
        // Generating mocks
        this.product = this.generateProductMock();
        List<Product> products = new ArrayList<>();
        products.add(product);
        DocSystemDto docSystemDto = this.generateDocSystemDtoMock();

        DocSystem parentDocument = new DocSystem();
        parentDocument.setCategory(DocumentCategory.GOOGLE_DRIVE);
        parentDocument.setType(DocumentType.FOLDER);

        DMDocumentFolderDTO[] documentFolders = new DMDocumentFolderDTO[1];
        DMDocumentFolderDTO dmDocumentFolderDTO = new DMDocumentFolderDTO();
        documentFolders[0] = dmDocumentFolderDTO;

        DMDocumentDTO dmDocumentDTO = new DMDocumentDTO();
        dmDocumentDTO.setFolders(documentFolders);

        DocSystem saveDocSystem = new DocSystem(
                DocSystemUtils.getChildFolderDocSystemName(dmDocumentFolderDTO.getName(), product),
                DocSystemUtils.getChildFolderDocSystemDescription(dmDocumentFolderDTO.getName(), product),
                dmDocumentFolderDTO.getUrl(),
                product,
                DocumentCategory.MSA,
                DocumentType.FOLDER,
                parentDocument,
                true
        );

        when(this.productRepository.findAll()).thenReturn(products);
        when(this.docSystemRepo.findByProduct(anyInt())).thenReturn(List.of(parentDocument));
        when(this.documentsManagerClient.createDocumentFoldersForProduct(any(), any())).thenReturn(dmDocumentDTO);
        when(this.docSystemRepo.findBySystemNameAndProductIdAndCategoryAndType(anyString(), anyInt(), any(), any())).thenReturn(Optional.empty());
        when(this.docSystemRepo.saveAndFlush(saveDocSystem)).thenReturn(saveDocSystem);

        this.docSystemApiService.createDocSystemProductsFolder(docSystemDto.getCategory());

        verify(this.docSystemRepo, times(1)).saveAndFlush(saveDocSystem);
    }

    @Test
    void createDocSystemProductsFolderError()
    {
        Assertions.assertThrows(NovaException.class, () -> this.docSystemApiService.createDocSystemProductsFolder("NO_CATEGORY"));
    }


    private DocSystemDto generateDocSystemDtoMock()
    {

        DocSystemDto docSystemDto = new DocSystemDto();
        docSystemDto.setId(1);
        docSystemDto.setDescription("DocSystem DTO mocked");
        docSystemDto.setSystemName("DocSystem DTO system name mocked");
        docSystemDto.setUrl("www.bbva.com");
        docSystemDto.setCategory("MSA");

        return docSystemDto;

    }

    private DocSystem generateDocSystemMock()
    {

        Product productMocked = new Product();
        productMocked.setId(1);
        productMocked.setName("productMock");
        productMocked.setUuaa("MOCK");

        DocSystem docSystemMocked = new DocSystem();
        docSystemMocked.setProduct(productMocked);
        docSystemMocked.setDescription("Mock for docSystem and its test");
        docSystemMocked.setSystemName("docSystemMocked");
        docSystemMocked.setUrl("www.bbva.com");
        docSystemMocked.setId(1);

        return docSystemMocked;

    }

    private Product generateProductMock()
    {

        Product productMocked = new Product();
        productMocked.setId(1);
        productMocked.setName("productMock");
        productMocked.setUuaa("MOCK");

        return productMocked;

    }

}
