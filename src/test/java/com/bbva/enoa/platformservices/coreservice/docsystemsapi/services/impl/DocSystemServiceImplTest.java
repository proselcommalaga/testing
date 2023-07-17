package com.bbva.enoa.platformservices.coreservice.docsystemsapi.services.impl;

import com.bbva.enoa.apirestgen.documentsmanagerapi.model.DMDocumentDTO;
import com.bbva.enoa.apirestgen.documentsmanagerapi.model.DMDocumentFolderDTO;
import com.bbva.enoa.apirestgen.documentsmanagerapi.model.DMDocumentsActionsDTO;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.datamodel.model.product.entities.DocSystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentCategory;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DocSystemRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IDocumentsManagerClient;
import com.bbva.enoa.platformservices.coreservice.docsystemsapi.exceptions.DocSystemErrorCode;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class DocSystemServiceImplTest
{

    @Mock
    private DocSystemRepository docSystemRepository;
    @Mock
    private IDocumentsManagerClient docsClient;
    @Mock
    private DocSystem docSystem;

    @InjectMocks
    private DocSystemServiceImpl docSystemService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createDefaultDocSystemTest()
    {

        USUserDTO usUserDTOMock = this.generateUsUserDTOMock();
        Product product = this.generateProductMock();
        this.docSystem = this.generateDocSystemMock();

        // Mocking DDBB calls to create and save the docSystem
        DMDocumentDTO dmDocumentDTO = this.generateDMDocumentDto();
        when(this.docsClient.createRoot(product, usUserDTOMock.getUserCode())).thenReturn(dmDocumentDTO);
        when(this.docSystemRepository.saveAndFlush(this.docSystem)).thenReturn(this.docSystem);

        // Call service method
        this.docSystemService.createDefaultDocSystem(product, usUserDTOMock);

        // Verifying test conclude on addUser call -> Calling options tested in next methods.
        verify(this.docsClient, times(1)).addUser(product, usUserDTOMock.getEmail(), usUserDTOMock.getUserCode());

    }

    @Test
    void createDefaultDocSystemTest_cannotBeConvertedToEnum()
    {

        USUserDTO usUserDTOMock = this.generateUsUserDTOMock();
        Product product = this.generateProductMock();
        this.docSystem = this.generateDocSystemMock();

        // Mocking DDBB calls to create and save the docSystem
        DMDocumentFolderDTO folderDto1 = new DMDocumentFolderDTO();
        folderDto1.setName("NOT A DOCUMENT CATEGORY");
        DMDocumentFolderDTO[] folderDtosArray = new DMDocumentFolderDTO[]{folderDto1};

        DMDocumentDTO dmDocumentDTO = this.generateDMDocumentDto();
        dmDocumentDTO.setFolders(folderDtosArray);
        when(this.docsClient.createRoot(product, usUserDTOMock.getUserCode())).thenReturn(dmDocumentDTO);
        when(this.docSystemRepository.saveAndFlush(this.docSystem)).thenReturn(this.docSystem);

        // Call service method
        this.docSystemService.createDefaultDocSystem(product, usUserDTOMock);

        // Verifying test did not conclude on addUser call.
        verify(this.docsClient, times(0)).addUser(product, usUserDTOMock.getEmail(), usUserDTOMock.getUserCode());

    }

    @Test
    void createDefaultDocSystemTestWrongArguments()
    {

        Product product = this.generateProductMock();

        // Runtime exception if null
        Assertions.assertThrows(RuntimeException.class, () -> this.docSystemService.createDefaultDocSystem(null, null));
        try
        {
            this.docSystemService.createDefaultDocSystem(product, null);
        }
        catch (RuntimeException e)
        {

            String errorMessage = "[DocSystemServiceServiceImpl] -> [createDefaultDocSystem]: error creating default doc system in product id: [{1}]. Error message: [{2}]";
            String productId = product.getId().toString();
            String message = e.getMessage();

            Assertions.assertTrue(e.getMessage().contains(this.messageErrorCheckGenerator(errorMessage, productId, message).getMessage()));

        }
    }

    @Test
    void addUserToDefaultDocSystemTest()
    {

        USUserDTO usUserDTOMock = this.generateUsUserDTOMock();
        Product product = this.generateProductMock();
        this.docSystem = this.generateDocSystemMock();

        this.docSystemService.addUserToDefaultDocSystem(product, usUserDTOMock, usUserDTOMock.getUserCode());

        verify(this.docsClient, times(1)).addUser(product, usUserDTOMock.getEmail(), usUserDTOMock.getUserCode());

    }

    @Test
    void addUserToDefaultDocSystemTestWrongArguments()
    {

        // Runtime exception if null
        Assertions.assertThrows(RuntimeException.class, () -> this.docSystemService.addUserToDefaultDocSystem(null, null, null));

    }


    @Test
    void removeUserFromDefaultDocSystemTest()
    {

        USUserDTO usUserDTOMock = this.generateUsUserDTOMock();
        Product product = this.generateProductMock();
        this.docSystem = this.generateDocSystemMock();

        this.docSystemService.removeUserFromDefaultDocSystem(product, usUserDTOMock, usUserDTOMock.getUserCode());

        verify(this.docsClient, times(1)).removeUser(product, usUserDTOMock.getEmail(), usUserDTOMock.getUserCode());

    }

    @Test
    void removeUserFromDefaultDocSystemTestWrongArguments()
    {

        // Runtime exception if null
        Assertions.assertThrows(RuntimeException.class, () -> this.docSystemService.removeUserFromDefaultDocSystem(null, null, null));
    }

    @Test
    void removeDefaultRepositoryTest()
    {

        USUserDTO usUserDTOMock = this.generateUsUserDTOMock();
        Product product = this.generateProductMock();
        this.docSystem = this.generateDocSystemMock();

        //Modifying the systemName to match with test case
        this.docSystem.setSystemName(product.getName() + " Google Drive");

        // Generate returned list
        List<DocSystem> docSystemList = new ArrayList<>();
        docSystemList.add(this.docSystem);

        when(this.docSystemRepository.findByProduct(product.getId())).thenReturn(docSystemList);

        this.docSystemService.removeDefaultRepository(product);

        // Verifying method was called due to the docSystemList obtained has at least one element with the same driveDocSystemName
        verify(this.docsClient, times(1)).removeRoot(product);

    }

    @Test
    void removeDefaultRepositoryTestWrongArgumentsSimple()
    {

        //Case with wrong arguments
        Assertions.assertThrows(RuntimeException.class, () -> this.docSystemService.removeDefaultRepository(null));
    }


    @Test
    void removeDefaultRepositoryTestWrongArgumentsComplex()
    {

        Product product = this.generateProductMock();
        this.docSystem = this.generateDocSystemMock();

        //Modifying the systemName to match with test case
        this.docSystem.setSystemName(product.getName() + " Google Drive");

        // Generate returned list
        List<DocSystem> docSystemList = new ArrayList<>();
        docSystemList.add(this.docSystem);

        when(this.docSystemRepository.findByProduct(product.getId())).thenReturn(docSystemList);

        doThrow(RuntimeException.class).when(this.docsClient).removeRoot(any(Product.class));

        try
        {
            this.docSystemService.removeDefaultRepository(product);
        }
        catch (RuntimeException r)
        {

            String errorMessage = "Error deleting docSystem for product with id: [{0}]. Error message: [{1}] ";
            String productId = product.getId().toString();
            String message = r.getMessage();

            Assertions.assertTrue(r.getMessage().contains(this.messageErrorCheckGenerator(errorMessage, productId, message).getMessage()));
        }

    }

    @Test
    public void getDocSystemWithIdAndCategoryAndType()
    {
        // Input data
        Integer docSystemId = 1;
        DocumentCategory documentCategory = DocumentCategory.ARA;
        DocumentType documentType = DocumentType.FILE;

        // Exercise
        this.docSystemService.getDocSystemWithIdAndCategoryAndType(docSystemId, documentCategory, documentType);

        // Verify
        verify(this.docSystemRepository, times(1)).findByIdAndCategoryAndType(docSystemId, documentCategory, documentType);
    }

    private USUserDTO generateUsUserDTOMock()
    {

        USUserDTO usUserDTOMock = new USUserDTO();
        usUserDTOMock.setUserName("Name");
        usUserDTOMock.setSurname1("Surname1");
        usUserDTOMock.setUserName("Surname2");
        usUserDTOMock.setActive(true);
        usUserDTOMock.setEmail("user@bbva.com");
        usUserDTOMock.setTeams(new String[]{"team1", "team2"});
        usUserDTOMock.setUserCode("E000000");


        return usUserDTOMock;

    }

    private Product generateProductMock()
    {

        Product productMocked = new Product();
        productMocked.setId(1);
        productMocked.setName("productMock");
        productMocked.setUuaa("MOCK");

        return productMocked;

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

    private DMDocumentDTO generateDMDocumentDto()
    {
        DMDocumentsActionsDTO actionsDto = new DMDocumentsActionsDTO();
        actionsDto.setViewUrl("www.bbva.com");
        actionsDto.setDeleteUrl("deleteUrl");
        actionsDto.setDownloadUrl("www.bbva.com");

        DMDocumentFolderDTO folderDto1 = new DMDocumentFolderDTO();
        folderDto1.setName(DocumentCategory.MSA.getName());
        folderDto1.setUrl("https://platform.bbva.com/1");
        DMDocumentFolderDTO[] folderDtosArray = new DMDocumentFolderDTO[]{folderDto1};

        DMDocumentDTO documentDto = new DMDocumentDTO();
        documentDto.setDescription("Mock for docSystem and its test");
        documentDto.setDocName("docSystemMocked");
        documentDto.setId("DocumentIdentifier");
        documentDto.setLastUpdatedDate("01/01/2021");
        documentDto.setMimeType("MimeType");
        documentDto.setActions(actionsDto);
        documentDto.setSizeKb(1L);
        documentDto.setFolders(folderDtosArray);

        return documentDto;

    }

    private FormattingTuple messageErrorCheckGenerator(String ... strings){

        FormattingTuple errorMessage = MessageFormatter.format("[DocSystemServiceServiceImpl] -> [createDefaultDocSystem]: error creating default doc system in product id: [{1}]. Error message: [{2}]", strings[1],
                strings[2]);

        return errorMessage;
    }


}
