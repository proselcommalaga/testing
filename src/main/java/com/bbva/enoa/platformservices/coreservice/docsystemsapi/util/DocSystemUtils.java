package com.bbva.enoa.platformservices.coreservice.docsystemsapi.util;

import com.bbva.enoa.datamodel.model.product.entities.DocSystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * Utilities related to DocSystems.
 */
@Service
@Slf4j
public class DocSystemUtils
{

    /**
     * Get the name for the Google Drive folder of a product.
     *
     * @param product The given product.
     * @return The name for the Google Drive folder of the product.
     */
    public static String getDriveDocSystemName(Product product)
    {
        return String.format("%s Google Drive", product.getName());
    }

    /**
     * Get the name for a child folder of a product.
     *
     * @param folder Some string that identifies the folder.
     * @param product The given product.
     * @return The name for the child folder of the product.
     */
    public static String getChildFolderDocSystemName(String folder, Product product)
    {
        return String.format("Carpeta '%s' del producto %s", folder, product.getUuaa());
    }

    /**
     * Get the name for a file of a product.
     *
     * @param file Some string that identifies the file.
     * @param product The given product.
     * @return The name for the file of the product.
     */
    public static String getFileDocSystemName(String file, Product product)
    {
        return String.format("%s del producto %s", file, product.getUuaa());
    }

    /**
     * Get the description for the Google Drive folder of a product.
     *
     * @param product The given product.
     * @return The description for the Google Drive folder of the product.
     */
    public static String getDriveDocSystemDescription(Product product)
    {
        return String.format("Sistema de Documentación del producto %s", product.getName());
    }

    /**
     * Get the description for a child folder of a product.
     *
     * @param folder Some String that identifies the folder.
     * @param product The given product.
     * @return The description for a child folder of the product.
     */
    public static String getChildFolderDocSystemDescription(String folder, Product product)
    {
        return String.format("Carpeta para '%s' del producto %s", folder, product.getName());
    }

    /**
     * Get the description for a file of a product.
     *
     * @param file Some String that identifies the file.
     * @param product The given product.
     * @return The description for a file of the product.
     */
    public static String getFileDocSystemDescription(String file, Product product)
    {
        return String.format("%s del producto %s", file, product.getName());
    }

    /**
     * Set the properties of a "resource document" with the values from a DocSystem entity.
     * The "resource document" is an object with some of the same fields as a DocSystem class.
     *
     * @param resourceDocument  The "resource document" object.
     * @param docSystem         The DocSystem entity.
     * @return The resourceDocument object, with the properties set.
     */
    public static <T> T fillResourceDocumentWithDocSystem(T resourceDocument, DocSystem docSystem)
    {
        BeanUtils.copyProperties(docSystem, resourceDocument);
        return resourceDocument;
    }

}
