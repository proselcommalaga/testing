package com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces;

import com.bbva.enoa.apirestgen.productsapi.model.ProductsAssignedResourcesReport;
import com.bbva.enoa.apirestgen.productsapi.model.ProductsFilesystemsUsageReportDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductsHostsReportDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductsUsedResourcesReportDTO;

/**
 * Service class used for the methods dedicated to Products Report
 * Created by XE69190 on 12/01/2018.
 */
public interface IProductReportService
{
    /**
     * Gets total and available resources by Product.
     *
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param uuaa        Product UUAA. If it's empty or absent, it's equals to 'ALL'.
     * @return Total and available resources by Product.
     */
    ProductsUsedResourcesReportDTO getProductsUsedResourcesReport(String environment, String uuaa);

    /**
     * Get a CSV or Excel with the total and available resources by Product.
     *
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param uuaa        Product UUAA. If it's empty or absent, it's equals to 'ALL'.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getProductsUsedResourcesReportExport(String environment, String uuaa, String format);

    /**
     * Get general filesystems usage info for products.
     *
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param uuaa        Product UUAA. If it's empty or absent, it's equals to 'ALL'.
     * @return General filesystem usage info for products.
     */
    ProductsFilesystemsUsageReportDTO getProductsFilesytemsUsageReport(String environment, String uuaa);

    /**
     * Get a CSV or Excel with the general filesystem usage info for products.
     *
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param uuaa        Product UUAA. If it's empty or absent, it's equals to 'ALL'.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getProductsFilesytemsUsageReportExport(String environment, String uuaa, String format);

    /**
     * Get memory info for product machines.
     *
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param cpd         The CPD (Tres Cantos, Vaguada). If it's empty or absent, it's equals to 'ALL'.
     * @return Memory info for product machines.
     */
    ProductsHostsReportDTO getProductsHostsReport(String environment, String cpd);

    /**
     * Get a CSV or Excel with the memory info for product machines.
     *
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param cpd         The CPD (Tres Cantos, Vaguada). If it's empty or absent, it's equals to 'ALL'.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getProductsHostsReportExport(String environment, String cpd, String format);

    /**
     * Gets assigned resources by Product.
     *
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param uuaa        Product UUAA. If it's empty or absent, it's equals to 'ALL'.
     * @param pageSize    Page size
     * @param pageNumber  Page number
     * @return Assigned resources by Product.
     */
    ProductsAssignedResourcesReport getProductsAssignedResourcesReport(String environment, String uuaa, Integer pageSize, Integer pageNumber);

    /**
     * Get a CSV or Excel with the assigned resources by Product.
     *
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param uuaa        Product UUAA. If it's empty or absent, it's equals to 'ALL'.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getProductsAssignedResourcesReportExport(String environment, String uuaa, String format);
}
