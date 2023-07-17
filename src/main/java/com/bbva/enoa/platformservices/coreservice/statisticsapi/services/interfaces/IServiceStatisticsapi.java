package com.bbva.enoa.platformservices.coreservice.statisticsapi.services.interfaces;

import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBHardwareBudgetSnapshot;
import com.bbva.enoa.apirestgen.statisticsapi.model.AlertsSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.ApiSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.AvailabilityNovaCoinsDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.BatchExecutionsSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.BatchSchedulerExecutionsSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.BrokersSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.BudgetSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.BuildJobSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.CategorySummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.ConnectorsSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.DeploymentPlansSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.FileTransfersInstancesSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.FileTransfersSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.FilesystemsSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.InstancesSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.ProductSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.QualityAnalysesSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.ReleaseVersionSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.STHistoricalPoint;
import com.bbva.enoa.apirestgen.statisticsapi.model.STHistoricalSerie;
import com.bbva.enoa.apirestgen.statisticsapi.model.ServicesSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.SubsystemSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.TODOTaskSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.UserSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsuserapi.model.UserProductRoleHistoryDTO;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemsCombinationDTO;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;

/**
 * The interface Service statisticsapi.
 */
public interface IServiceStatisticsapi
{
    /**
     * Gets products number.
     *
     * @return Get the products number in Production
     */
    Long getProductsNumber();

    /**
     * Gets services number.
     *
     * @return Get the services number in Production
     */
    Long getServicesNumber();

    /**
     * Gets apis number in pro.
     *
     * @return Get the APIs number in Production
     */
    Long getApisNumberInPro();

    /**
     * Gets users number.
     *
     * @return Get the users number in Production
     */
    Long getUsersNumber();

    /**
     * Get the error codes of all NOVA Platform services
     *
     * @return Get the error codes of all NOVA Platform services
     */
    String getErrorCodes();

    /**
     * Get statistics related to the Release Versions stored in the platform, such as the total number or what type of services
     * they contain. The results can be filtered by UUAA and status of the Release Version.
     *
     * @param uuaa                 Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param platform             the platform
     * @param releaseVersionStatus Filter the results for a specific Release Version status. If its equals to "ALL", or it's null, or it's empty, no status filtering is applied.
     * @return A ReleaseVersionSummaryDTO.
     */
    ReleaseVersionSummaryDTO getReleaseVersionsSummary(final String uuaa, final String platform, final String releaseVersionStatus);

    /**
     * Get a CSV or Excel with statistics related to the Release Versions stored in the platform, such as the total number or what type of services
     * they contain. The results can be filtered by UUAA and status of the Release Version.
     *
     * @param uuaa     Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param platform the platform
     * @param status   Filter the results for a specific Release Version status. If its equals to "ALL", or it's null, or it's empty, no status filtering is applied.
     * @param format   Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getReleaseVersionsSummaryExport(String uuaa, String platform, String status, String format);

    /**
     * Get statistics related to the Subsystems stored in the platform, such as the total number or their type.
     * The results can be filtered by UUAA.
     *
     * @param uuaa Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @return A SubsystemSummaryDTO.
     */
    SubsystemSummaryDTO getSubsystemsSummary(final String uuaa);

    /**
     * Get a CSV or Excel with statistics related to the Subsystems stored in the platform, such as the total number or their type.
     * The results can be filtered by UUAA.
     *
     * @param uuaa   Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param format Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getSubsystemsSummaryExport(String uuaa, String format);

    /**
     * Get statistics related to the Users stored in the platform, such as the total number or their teams.
     * The results can be filtered by UUAA.
     *
     * @param uuaa Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @return A UserSummaryDTO
     */
    UserSummaryDTO getUsersSummary(final String uuaa);

    /**
     * Get a CSV or Excel with statistics related to the Users stored in the platform, such as the total number or their teams.
     * The results can be filtered by UUAA.
     *
     * @param uuaa   Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param format Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getUsersSummaryExport(String uuaa, String format);

    /**
     * Get statistics related to the Tasks stored in the platform, such as the total number or their type.
     * The results can be filtered by UUAA and Status (see {@link ToDoTaskType}).
     *
     * @param uuaa   Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param status Filter the results for a specific Status (see {@link ToDoTaskType}). If its equals to "ALL", or it's null, or it's empty, no status filtering is applied.
     * @return A TODOTaskSummaryDTO.
     */
    TODOTaskSummaryDTO getTodotasksSummary(final String uuaa, final String status);

    /**
     * Get a CSV or Excel with statistics related to the Tasks stored in the platform, such as the total number or their type.
     * The results can be filtered by UUAA and Status (see {@link ToDoTaskType}).
     *
     * @param uuaa   Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param status Filter the results for a specific Status (see {@link ToDoTaskType}). If its equals to "ALL", or it's null, or it's empty, no status filtering is applied.
     * @param format Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getTodotasksSummaryExport(String uuaa, String status, String format);

    /**
     * Get statistics related to products stored in the platform, such as the total number of their type
     *
     * @return a ProductSummaryDTO
     */
    ProductSummaryDTO getProductsSummary();

    /**
     * Get a CSV or Excel with statistics related to products stored in the platform, such as the total number of their type
     *
     * @param format Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getProductsSummaryExport(String format);

    /**
     * Get statistics related to the Build Jobs stored in the platform, such as the total number or their Status.
     * The results can be filtered by UUAA and Type.
     *
     * @param jobType Filter the results for a specific Type. If it's equals to "ALL", or it's null, or it's empty, no Type filtering is applied.
     * @param uuaa    Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @return A BuildJobSummaryDTO.
     */
    BuildJobSummaryDTO getBuildJobsSummary(final String jobType, final String uuaa);

    /**
     * Get a CSV or Excel with statistics related to the Build Jobs stored in the platform, such as the total number or their Status.
     * The results can be filtered by UUAA and Type.
     *
     * @param jobType Filter the results for a specific Type. If it's equals to "ALL", or it's null, or it's empty, no Type filtering is applied.
     * @param uuaa    Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param format  Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getBuildJobsSummaryExport(String jobType, String uuaa, String format);

    /**
     * Get statistics related to the Categories stored in the platform, such as the total number or their UUAAs.
     * The results can be filtered by the Name of the Category.
     *
     * @param categoryName Filter the results for a specific Category Name. If it's null, or it's empty, no Category Name filtering is applied.
     * @return A CategorySummaryDTO.
     */
    CategorySummaryDTO getCategoriesSummary(final String categoryName);

    /**
     * Get a CSV or Excel with statistics related to the Categories stored in the platform, such as the total number or their UUAAs.
     * The results can be filtered by the Name of the Category.
     *
     * @param categoryName Filter the results for a specific Category Name. If it's null, or it's empty, no Category Name filtering is applied.
     * @param format       Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getCategoriesSummaryExport(String categoryName, String format);

    /**
     * Get statistics related to the APIs stored in the platform, such as the total number or their "Type" (governed, external, etc.).
     * The results can be filtered by UUAA and "Status" ("deployed", "in definition", etc.).
     *
     * @param environment   Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param functionality Filter the results for a specific api funcionality (Sync, Async_backToFront, Async_backToBack)
     * @param uuaa          Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @return An ApiSummaryDTO.
     */
    ApiSummaryDTO getApisSummary(final String environment, String functionality, final String uuaa);

    /**
     * Get a CSV or Excel with statistics related to the APIs stored in the platform, such as the total number or their "Type" (governed, external, etc.).
     * The results can be filtered by UUAA and "Status" ("deployed", "in definition", etc.).
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getApisSummaryExport(String environment, String uuaa, String format);

    /**
     * Get statistics related to the Quality Analyses stored in the platform, such as the total number or their "SQA state" (OK or ERROR).
     *
     * @return A QualityAnalysesSummaryDTO.
     */
    QualityAnalysesSummaryDTO getQualityAnalysesSummary();

    /**
     * Get a CSV or Excel with statistics related to the Quality Analyses stored in the platform, such as the total number or their "SQA state" (OK or ERROR).
     *
     * @param format Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getQualityAnalysesSummaryExport(String format);

    /**
     * Get statistics related to the Budgets stored in the platform, such as the total number of Initiatives and NOVA coins.
     * The results can be filtered by UUAA and "Status".
     *
     * @param uuaa   Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param status Filter the results for a specific Status. If it's equals to "ALL", or it's null, or it's empty, no Status filtering is applied.
     * @return A BudgetSummaryDTO.
     */
    BudgetSummaryDTO getBudgetsSummary(final String uuaa, final String status);

    /**
     * Get a CSV or Excel with statistics related to the Budgets stored in the platform, such as the total number of Initiatives and NOVA coins.
     * The results can be filtered by UUAA and "Status".
     *
     * @param uuaa   Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param status Filter the results for a specific Status. If it's equals to "ALL", or it's null, or it's empty, no Status filtering is applied.
     * @param format Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getBudgetsSummaryExport(String uuaa, String status, String format);

    /**
     * Get statistics related to the Connectors stored in the platform, such as the total number or their type.
     * The results can be filtered by Environment, UUAA and "Status".
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param status      Filter the results for a specific Status. If it's equals to "ALL", or it's null, or it's empty, no Status filtering is applied.
     * @return A ConnectorsSummaryDTO.
     */
    ConnectorsSummaryDTO getConnectorsSummary(final String environment, final String uuaa, final String status);

    /**
     * Get a CSV or Excel with statistics related to the Connectors stored in the platform, such as the total number or their type.
     * The results can be filtered by Environment, UUAA and "Status".
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param status      Filter the results for a specific Status. If it's equals to "ALL", or it's null, or it's empty, no Status filtering is applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getConnectorsSummaryExport(String environment, String uuaa, String status, String format);

    /**
     * Get statistics related to the Services deployed in the platform, such as the total number or their type.
     * The results can be filtered by Environment, UUAA and Platform.
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param platform    Filter the results for a specific Platform. If it's equals to "ALL", or it's null, or it's empty, no Platform filtering is applied.
     * @return A ServicesSummaryDTO.
     */
    ServicesSummaryDTO getServicesSummary(final String environment, final String uuaa, final String platform);

    /**
     * Get a CSV or Excel with statistics related to the Services deployed in the platform, such as the total number or their type.
     * The results can be filtered by Environment, UUAA and Platform.
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param platform    Filter the results for a specific Platform. If it's equals to "ALL", or it's null, or it's empty, no Platform filtering is applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getServicesSummaryExport(String environment, String uuaa, String platform, String format);

    /**
     * Get statistics related to the Deployment Plans stored in the platform, such as the total number or their status.
     * The results can be filtered by Environment, UUAA and Platform.
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param platform    Filter the results for a specific Platform. If it's equals to "ALL", or it's null, or it's empty, no Platform filtering is applied.
     * @return A DeploymentPlansSummaryDTO.
     */
    DeploymentPlansSummaryDTO getDeploymentPlansSummary(final String environment, final String uuaa, final String platform);

    /**
     * Get a CSV or Excel with statistics related to the Deployment Plans stored in the platform, such as the total number or their status.
     * The results can be filtered by Environment, UUAA and Platform.
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param platform    Filter the results for a specific Platform. If it's equals to "ALL", or it's null, or it's empty, no Platform filtering is applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getDeploymentPlansSummaryExport(String environment, String uuaa, String platform, String format);

    /**
     * Get statistics related to the Alerts stored in the platform, such as the total number or their status.
     * The results can be filtered by Environment, UUAA and Platform.
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param type        Filter the results for a specific Type. If it's equals to "ALL", or it's null, or it's empty, no Type filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @return A AlertsSummaryDTO.
     */
    AlertsSummaryDTO getAlertsSummary(final String environment, final String type, final String uuaa);

    /**
     * Get a CSV or Excel with statistics related to the Alerts stored in the platform, such as the total number or their status.
     * The results can be filtered by Environment, UUAA and Platform.
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param type        Filter the results for a specific Type. If it's equals to "ALL", or it's null, or it's empty, no Type filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getAlertsSummaryExport(String environment, String type, String uuaa, String format);

    /**
     * Get statistics related to the Filesystems stored in the platform, such as the total number or their type	.
     * The results can be filtered by Environment, UUAA and Status.
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param status      Filter the results for a specific Status. If it's equals to "ALL", or it's null, or it's empty, no Status filtering is applied.
     * @return A FilesystemsSummaryDTO.
     */
    FilesystemsSummaryDTO getFilesystemsSummary(final String environment, final String uuaa, final String status);

    /**
     * Get a CSV or Excel with statistics related to the Filesystems stored in the platform, such as the total number or their type	.
     * The results can be filtered by Environment, UUAA and Status.
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param status      Filter the results for a specific Status. If it's equals to "ALL", or it's null, or it's empty, no Status filtering is applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getFilesystemsSummaryExport(String environment, String uuaa, String status, String format);

    /**
     * Get statistics related to the File Transfer Configurations stored in the platform, such as the total number or their status.	.
     * The results can be filtered by Environment and UUAA.
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @return A FileTransfersSummaryDTO.
     */
    FileTransfersSummaryDTO getFileTransfersSummary(final String environment, final String uuaa);

    /**
     * Get a CSV or Excel with statistics related to the File Transfer Configurations stored in the platform, such as the total number or their status.	.
     * The results can be filtered by Environment and UUAA.
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getFileTransfersSummaryExport(String environment, String uuaa, String format);

    /**
     * Get statistics related to the File Transfer Instances stored in the platform, such as the total number or their status.	.
     * The results can be filtered by Environment and UUAA.
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @return A FileTransfersInstancesSummaryDTO.
     */
    FileTransfersInstancesSummaryDTO getFileTransfersInstancesSummary(final String environment, final String uuaa);

    /**
     * Get a CSV or Excel with statistics related to the File Transfer Instances stored in the platform, such as the total number or their status.	.
     * The results can be filtered by Environment and UUAA.
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getFileTransfersInstancesSummaryExport(String environment, String uuaa, String format);

    /**
     * Get summarized information for batch instance executions. It returns total number of tasks, and number of tasks by task status.
     * The results can be filtered by Environment, UUAA, platform and origin (initiator).
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param platform    Filter the results for a specific Platform. If it's equals to "ALL", or it's null, or it's empty, no Platform filtering is applied.
     * @param origin      Filter the results for a specific Origin (Control-M, On demand or NOVA schedule). If it's equal to "ALL", no Origin filtering is applied.
     * @return A BatchExecutionsSummaryDTO.
     */
    BatchExecutionsSummaryDTO getBatchExecutionsSummary(final String environment, final String uuaa, final String platform, final String origin);

    /**
     * Get a CSV or Excel with summarized information for batch instance executions. It returns total number of tasks, and number of tasks by task status.
     * The results can be filtered by Environment, UUAA, platform and origin (initiator).
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param platform    Filter the results for a specific Platform. If it's equals to "ALL", or it's null, or it's empty, no Platform filtering is applied.
     * @param origin      Filter the results for a specific Origin (Control-M, On demand or NOVA schedule). If it's equal to "ALL", no Origin filtering is applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getBatchExecutionsSummaryExport(String environment, String uuaa, String platform, String origin, String format);

    /**
     * Get summarized information for batch scheduled executions. It returns total number of tasks, and number of tasks by task status.
     * The results can be filtered by Environment, UUAA, platform and origin (initiator).
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param platform    Filter the results for a specific Platform. If it's equals to "ALL", or it's null, or it's empty, no Platform filtering is applied.
     * @return A {@link BatchExecutionsSummaryDTO}.
     */
    BatchSchedulerExecutionsSummaryDTO getBatchSchedulerExecutionsSummary(final String environment, final String uuaa, final String platform);

    /**
     * Get a CSV or Excel with summarized information for batch scheduled executions. It returns total number of tasks, and number of tasks by task status.
     * The results can be filtered by Environment, UUAA, platform and origin (initiator).
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param platform    Filter the results for a specific Platform. If it's equals to "ALL", or it's null, or it's empty, no Platform filtering is applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getBatchSchedulerExecutionsSummaryExport(String environment, String uuaa, String platform, String format);

    /**
     * Get statistics related to the Instances deployed in the platform, such as the total number or their service type.
     * The results can be filtered by Environment, UUAA, Platform and Status.
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param platform    Filter the results for a specific Platform. If it's equals to "ALL", or it's null, or it's empty, no Platform filtering is applied.
     * @param status      Filter the results for a specific Status. If it's equals to "ALL", or it's null, or it's empty, no Status filtering is applied.
     * @return A InstancesSummaryDTO.
     */
    InstancesSummaryDTO getInstancesSummary(final String environment, final String uuaa, final String platform, final String status);

    /**
     * Get a CSV or Excel with statistics related to the Instances deployed in the platform, such as the total number or their service type.
     * The results can be filtered by Environment, UUAA, Platform and Status.
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param platform    Filter the results for a specific Platform. If it's equals to "ALL", or it's null, or it's empty, no Platform filtering is applied.
     * @param status      Filter the results for a specific Status. If it's equals to "ALL", or it's null, or it's empty, no Status filtering is applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getInstancesSummaryExport(String environment, String uuaa, String platform, String status, String format);

    /**
     * Get the number of products for each day in the given range and for the given filters.
     *
     * @param startDate Start date, e.g. "2020-01-01".
     * @param endDate   End date, e.g. "2020-12-01".
     * @param type      Filter by this type of product ("NOVA", "LIBRARY").
     * @return An array of date-value pairs.
     */
    STHistoricalPoint[] getProductsHistorical(String startDate, String endDate, String type);

    /**
     * Get a CSV or Excel with the number of products for each day in the given range and for the given filters.
     *
     * @param startDate Start date, e.g. "2020-01-01".
     * @param endDate   End date, e.g. "2020-12-01".
     * @param type      Filter by this type of product ("NOVA", "LIBRARY").
     * @param format    Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getProductsHistoricalExport(String startDate, String endDate, String type, String format);

    /**
     * Get the number of deployed services for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param platform    Platform (ETHER or NOVA). If it's 'ALL', no Platform filtering will be applied.
     * @param language    Version Language. If it's 'ALL', no Type filtering will be applied.
     * @param type        Deployed Service Type. If it's 'ALL', no Type filtering will be applied.
     * @param uuaa        Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @param category    Each category to show in the chart.
     * @return An array of date-value pairs.
     */
    STHistoricalSerie[] getDeployedServicesHistorical(String startDate, String endDate, String environment, String platform, String language, String type, String uuaa, String category);

    /**
     * Get a CSV or Excel with the number of deployed services for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param platform    Platform (ETHER or NOVA). If it's 'ALL', no Platform filtering will be applied.
     * @param language    Version Language. If it's 'ALL', no Type filtering will be applied.
     * @param type        Deployed Service Type. If it's 'ALL', no Type filtering will be applied.
     * @param uuaa        Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @param format      Format of the file CSV or Excel
     * @param category    Each category to show in the chart.
     * @return bytes with the file
     */
    byte[] getDeployedServicesHistoricalExport(String startDate, String endDate, String environment, String platform, String language, String type, String uuaa, String format, String category);

    /**
     * Get the number of instances for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param platform    Platform (ETHER or NOVA). If it's 'ALL', no Platform filtering will be applied.
     * @param type        Instance Type. If it's 'ALL', no Type filtering will be applied.
     * @param uuaa        Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @return An array of date-value pairs.
     */
    STHistoricalSerie[] getInstancesHistorical(String startDate, String endDate, String environment, String platform, String type, String uuaa);

    /**
     * Get a CSV or Excel with the number of instances for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param platform    Platform (ETHER or NOVA). If it's 'ALL', no Platform filtering will be applied.
     * @param type        Instance Type. If it's 'ALL', no Type filtering will be applied.
     * @param uuaa        Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getInstancesHistoricalExport(String startDate, String endDate, String environment, String platform, String type, String uuaa, String format);

    /**
     * Get the number of categories for each day in the given range and for the given filters.
     *
     * @param startDate Start date, e.g. "2020-01-01".
     * @param endDate   End date, e.g. "2020-12-01".
     * @param type      Category Type. If it's 'ALL', no Type filtering will be applied.
     * @return An array of date-value pairs.
     */
    STHistoricalPoint[] getCategoriesHistorical(String startDate, String endDate, String type);

    /**
     * Get a CSV or Excel with the number of categories for each day in the given range and for the given filters.
     *
     * @param startDate Start date, e.g. "2020-01-01".
     * @param endDate   End date, e.g. "2020-12-01".
     * @param type      Category Type. If it's 'ALL', no Type filtering will be applied.
     * @param format    Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getCategoriesHistoricalExport(String startDate, String endDate, String type, String format);

    /**
     * Get the number of users for each day in the given range and for the given filters.
     * There are some differences with the rest of history query methods:
     * <p>
     * 1. As we are interested in unique users, if neither role nor uuaa are passed,
     * results will be grouped by unique user. If some user belongs to more than one
     * product, it will be added just once. Both of values will be converted to "ALL"
     * as plain string values. In history load process, some parameters are stored with "ALL"
     * value for some edge cases.
     * 2. If just a role is passed, results will be grouped by unique user as well. If
     * some user exists with same role in more than one product, it will be added just once.
     * Uuaa will be converted to "ALL" as plain string value. In history load process, some parameters are
     * stored with "ALL" value for some edge cases.
     * 3. If role and uuaa are passed, then query will apply given values as usual.
     * 4. If just an uuaa is passed, as it's not possible for a user to exist more than
     * once for a given product, query will apply given values as usual.
     *
     * @param startDate Start date, e.g. 2020-01-01.
     * @param endDate   End date, e.g. 2020-12-01.
     * @param role      User Role. If it's 'ALL', no Role filtering will be applied, depending on uuaa parameter value.
     * @param uuaa      Product UUAA. If it's 'ALL', no UUAA filtering will be applied, depending on role parameter value.
     * @return An array of date-value pairs.
     */
    STHistoricalPoint[] getUsersHistorical(String startDate, String endDate, String role, String uuaa);

    /**
     * Get a CSV or Excel with the number of users for each day in the given range and for the given filters.
     * There are some differences with the rest of history query methods:
     * <p>
     * 1. As we are interested in unique users, if neither role nor uuaa are passed,
     * results will be grouped by unique user. If some user belongs to more than one
     * product, it will be added just once. Both of values will be converted to "ALL"
     * as plain string values. In history load process, some parameters are stored with "ALL"
     * value for some edge cases.
     * 2. If just a role is passed, results will be grouped by unique user as well. If
     * some user exists with same role in more than one product, it will be added just once.
     * Uuaa will be converted to "ALL" as plain string value. In history load process, some parameters are
     * stored with "ALL" value for some edge cases.
     * 3. If role and uuaa are passed, then query will apply given values as usual.
     * 4. If just an uuaa is passed, as it's not possible for a user to exist more than
     * once for a given product, query will apply given values as usual.
     *
     * @param startDate Start date, e.g. 2020-01-01.
     * @param endDate   End date, e.g. 2020-12-01.
     * @param role      User Role. If it's 'ALL', no Role filtering will be applied, depending on uuaa parameter value.
     * @param uuaa      Product UUAA. If it's 'ALL', no UUAA filtering will be applied, depending on role parameter value.
     * @param format    Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getUsersHistoricalExport(String startDate, String endDate, String role, String uuaa, String format);

    /**
     * Get the number of connectors for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param status      Status of the connector. If it's 'ALL', no Status filtering will be applied.
     * @param type        Connector Type. If it's 'ALL', no Type filtering will be applied.
     * @param uuaa        Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @return An array of date-value pairs.
     */
    STHistoricalSerie[] getConnectorsHistorical(String startDate, String endDate, String environment, String status, String type, String uuaa);

    /**
     * Get a CSV or Excel with the number of connectors for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param status      Status of the connector. If it's 'ALL', no Status filtering will be applied.
     * @param type        Connector Type. If it's 'ALL', no Type filtering will be applied.
     * @param uuaa        Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getConnectorsHistoricalExport(String startDate, String endDate, String environment, String status, String type, String uuaa, String format);

    /**
     * Get the number of filesystems for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param status      Status of the filesystem. If it's 'ALL', no Status filtering will be applied.
     * @param type        Filesystem Type. If it's 'ALL', no Type filtering will be applied.
     * @param uuaa        Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @return An array of date-value pairs.
     */
    STHistoricalSerie[] getFilesystemsHistorical(String startDate, String endDate, String environment, String status, String type, String uuaa);

    /**
     * Get a CSV or Excel with the number of filesystems for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param status      Status of the filesystem. If it's 'ALL', no Status filtering will be applied.
     * @param type        Filesystem Type. If it's 'ALL', no Type filtering will be applied.
     * @param uuaa        Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getFilesystemsHistoricalExport(String startDate, String endDate, String environment, String status, String type, String uuaa, String format);

    /**
     * Get the number of subsystems for each day in the given range and for the given filters.
     *
     * @param startDate Start date, e.g. 2020-01-01.
     * @param endDate   End date, e.g. 2020-12-01.
     * @param type      Subsystem Type. If it's 'ALL', no Type filtering will be applied.
     * @param uuaa      Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @return An array of date-value pairs.
     */
    STHistoricalSerie[] getSubsystemsHistorical(String startDate, String endDate, String type, String uuaa);

    /**
     * Get a CSV or Excel with the number of subsystems for each day in the given range and for the given filters.
     *
     * @param startDate Start date, e.g. 2020-01-01.
     * @param endDate   End date, e.g. 2020-12-01.
     * @param type      Subsystem Type. If it's 'ALL', no Type filtering will be applied.
     * @param uuaa      Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @param format    Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getSubsystemsHistoricalExport(String startDate, String endDate, String type, String uuaa, String format);

    /**
     * Get the number of apis for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param type        Api Type. If it's 'ALL', no Type filtering will be applied.
     * @param uuaa        Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @param environment API environment. If it's 'ALL', no environment will be applied.
     * @return An array of date-value pairs.
     */
    STHistoricalSerie[] getApisHistorical(String apiFunctionality, String environment, String endDate, String uuaa, String type, String startDate);

    /**
     * Get a CSV or Excel with the number of apis for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param type        Api Type. If it's 'ALL', no Type filtering will be applied.
     * @param uuaa        Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @param environment API environment. If it's 'ALL', no environment will be applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getApisHistoricalExport(String startDate, String endDate, String type, String uuaa, String environment, String format);

    /**
     * Get the number of compilations for each day in the given range and for the given filters.
     *
     * @param startDate Start date, e.g. 2020-01-01.
     * @param endDate   End date, e.g. 2020-12-01.
     * @param status    Status of the compilation. If it's 'ALL', no Status filtering will be applied.
     * @param type      Compilation Type. If it's 'ALL', no Type filtering will be applied.
     * @param uuaa      Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @return An array of date-value pairs.
     */
    STHistoricalSerie[] getCompilationsHistorical(String startDate, String endDate, String status, String type, String uuaa);

    /**
     * Get a CSV or Excel with the number of compilations for each day in the given range and for the given filters.
     *
     * @param startDate Start date, e.g. 2020-01-01.
     * @param endDate   End date, e.g. 2020-12-01.
     * @param status    Status of the compilation. If it's 'ALL', no Status filtering will be applied.
     * @param type      Compilation Type. If it's 'ALL', no Type filtering will be applied.
     * @param uuaa      Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @param format    Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getCompilationsHistoricalExport(String startDate, String endDate, String status, String type, String uuaa, String format);

    /**
     * Get the number of filetransfers for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param status      Status of the filetransfer. If it's 'ALL', no Status filtering will be applied.
     * @param uuaa        Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @return An array of date-value pairs.
     */
    STHistoricalSerie[] getFiletransfersHistorical(String startDate, String endDate, String environment, String status, String uuaa);

    /**
     * Get a CSV or Excel with the number of filetransfers for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param status      Status of the filetransfer. If it's 'ALL', no Status filtering will be applied.
     * @param uuaa        Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getFiletransfersHistoricalExport(String startDate, String endDate, String environment, String status, String uuaa, String format);

    /**
     * Get the number of todotasks for each day in the given range and for the given filters.
     *
     * @param startDate Start date, e.g. 2020-01-01.
     * @param endDate   End date, e.g. 2020-12-01.
     * @param status    Status of the todotask. If it's 'ALL', no Status filtering will be applied.
     * @param type      Todotask Type. If it's 'ALL', no Type filtering will be applied.
     * @param role      Todotask Role. If it's 'ALL', no Role filtering will be applied.
     * @param uuaa      Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @return An array of date-value pairs.
     */
    STHistoricalPoint[] getTodotasksHistorical(String startDate, String endDate, String status, String type, String role, String uuaa);

    /**
     * Get a CSV or Excel with the number of todotasks for each day in the given range and for the given filters.
     *
     * @param startDate Start date, e.g. 2020-01-01.
     * @param endDate   End date, e.g. 2020-12-01.
     * @param status    Status of the todotask. If it's 'ALL', no Status filtering will be applied.
     * @param type      Todotask Type. If it's 'ALL', no Type filtering will be applied.
     * @param role      Todotask Role. If it's 'ALL', no Role filtering will be applied.
     * @param uuaa      Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @param format    Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getTodotasksHistoricalExport(String startDate, String endDate, String status, String type, String role, String uuaa, String format);

    /**
     * Get the number of memory for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param cpd         Cpd of the memory. If it's 'ALL', no Cpd filtering will be applied.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param unit        Unit of the memory. If it's 'ALL', no Unit filtering will be applied.
     * @return An array of date-value pairs.
     */
    STHistoricalPoint[] getMemoryHistorical(String startDate, String endDate, String cpd, String environment, String unit);

    /**
     * Get a CSV or Excel with the number of memory for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param cpd         Cpd of the memory. If it's 'ALL', no Cpd filtering will be applied.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param unit        Unit of the memory. If it's 'ALL', no Unit filtering will be applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getMemoryHistoricalExport(String startDate, String endDate, String cpd, String environment, String unit, String format);

    /**
     * Get the number of hardware for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param property    Property of the hardware. If it's 'ALL', no Property filtering will be applied.
     * @param uuaa        Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @return An array of date-value pairs.
     */
    STHistoricalSerie[] getHardwareHistorical(String startDate, String endDate, String environment, String property, String uuaa);

    /**
     * Get a CSV or Excel with the number of hardware for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param property    Property of the hardware. If it's 'ALL', no Property filtering will be applied.
     * @param uuaa        Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getHardwareHistoricalExport(String startDate, String endDate, String environment, String property, String uuaa, String format);

    /**
     * Get the number of storage for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param property    Property of the storage. If it's 'ALL', no Property filtering will be applied.
     * @param uuaa        Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @return An array of date-value pairs.
     */
    STHistoricalSerie[] getStorageHistorical(String startDate, String endDate, String environment, String property, String uuaa);

    /**
     * Get a CSV or Excel with the number of storage for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param property    Property of the storage. If it's 'ALL', no Property filtering will be applied.
     * @param uuaa        Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getStorageHistoricalExport(String startDate, String endDate, String environment, String property, String uuaa, String format);

    /**
     * Gets an array of (product_id, role, uuaa, occurrences) tuples represented by DTOs. Method for statistic history loading purposes.
     *
     * @return an array of (product_id, role, uuaa, occurrences) tuples represented by DTOs.
     */
    UserProductRoleHistoryDTO[] getUserProductRoleHistorySnapshot();

    /**
     * Gets an array of DTOs with hardware filters for statistic history loading purposes.
     *
     * @return an array of DTOs with hardware filters for statistic history loading purposes.
     */
    PBHardwareBudgetSnapshot[] getHardwareBudgetHistorySnapshot();

    /**
     * Gets an array of DTOs containing subsystems parameters for statistic history loading.
     *
     * @return an array of DTOs containing subsystems parameters for statistic history loading.
     */
    TOSubsystemsCombinationDTO[] getSubsystemsHistorySnapshot();

    /**
     * Get the number of deployed plans for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param status      Status of the deployed plans. If it's 'ALL', no Status filtering will be applied.
     * @param uuaa        Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @return An array of date-value pairs.
     */
    STHistoricalSerie[] getDeployedPlansHistorical(String startDate, String endDate, String environment, String status, String uuaa);

    /**
     * Get a CSV or Excel with the number of deployed plans for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param status      Status of the deployed plans. If it's 'ALL', no Status filtering will be applied.
     * @param uuaa        Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getDeployedPlansHistoricalExport(String startDate, String endDate, String environment, String status, String uuaa, String format);

    /**
     * Get the number of release versions for each day in the given range and for the given filters.
     *
     * @param startDate Start date, e.g. 2020-01-01.
     * @param endDate   End date, e.g. 2020-12-01.
     * @param status    Status of the release versions. If it's 'ALL', no Status filtering will be applied.
     * @param uuaa      Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @return An array of date-value pairs.
     */
    STHistoricalSerie[] getReleaseVersionsHistorical(String startDate, String endDate, String status, String uuaa);

    /**
     * Get a CSV or Excel with the number of release versions for each day in the given range and for the given filters.
     *
     * @param startDate Start date, e.g. 2020-01-01.
     * @param endDate   End date, e.g. 2020-12-01.
     * @param status    Status of the release versions. If it's 'ALL', no Status filtering will be applied.
     * @param uuaa      Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @param format    Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getReleaseVersionsHistoricalExport(String startDate, String endDate, String status, String uuaa, String format);

    /**
     * Get the number of batch instances executed for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param platform    Platform (ETHER or NOVA). If it's 'ALL', no Platform filtering will be applied.
     * @param uuaa        Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @param status      Batch ending status. If it's 'ALL', no status filtering will be applied.
     * @return An array of date-value pairs.
     */
    STHistoricalSerie[] getBatchExecutionsHistorical(String startDate, String endDate, String environment, String platform, String uuaa, String status);

    /**
     * Get a CSV or Excel with the number of batch instances executed for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param platform    Platform (ETHER or NOVA). If it's 'ALL', no Platform filtering will be applied.
     * @param uuaa        Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @param status      Batch ending status. If it's 'ALL', no status filtering will be applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getBatchExecutionsHistoricalExport(String startDate, String endDate, String environment, String platform, String uuaa, String status, String format);

    /**
     * Returns an array with DTOs representing used and available NOVA coins, grouped by environment.
     *
     * @param uuaa       The product uuaa.
     * @param budgetType Budget type. Options are HARDWARE (cpu and memory) and FILESYSTEM. If no budget type is provided, it will return NOVA coins for both of types.
     * @return An array with DTOs representing used and available NOVA coins, grouped by environment.
     */
    AvailabilityNovaCoinsDTO[] getNovaCoinsByAvailability(String uuaa, String budgetType);

    /**
     * Get a CSV or Excel with the used and available NOVA coins, grouped by environment.
     *
     * @param uuaa       The product uuaa.
     * @param budgetType Budget type. Options are HARDWARE (cpu and memory) and FILESYSTEM. If no budget type is provided, it will return NOVA coins for both of types.
     * @param format     Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getNovaCoinsByAvailabilityExport(String uuaa, String budgetType, String format);

    /**
     * Get the batch adoption level for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param platform    Platform (ETHER or NOVA). If it's 'ALL', no Platform filtering will be applied.
     * @return An array of date-value pairs.
     */
    STHistoricalSerie[] getAdoptionLevelHistorical(String startDate, String endDate, String environment, String platform);

    /**
     * Get a CSV or Excel with the batch adoption level for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param platform    Platform (ETHER or NOVA). If it's 'ALL', no Platform filtering will be applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getAdoptionLevelHistoricalExport(String startDate, String endDate, String environment, String platform, String format);

    /**
     * Get statistics related to products stored in the platform, such as their deployed platform
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param uuaa        The product uuaa.
     * @return a STHistoricalSerie
     */
    STHistoricalSerie[] getCloudProductsSummary(String startDate, String endDate, String environment, String uuaa);

    /**
     * Get a CSV or Excel with statistics related to products stored in the platform, such as their deployed platform
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param uuaa        The product uuaa.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getCloudProductsSummaryExport(String startDate, String endDate, String environment, String uuaa, String format);

    /**
     * Get statistics related to connected users stored in the platform
     *
     * @param startDate Start date, e.g. 2020-01-01.
     * @param endDate   End date, e.g. 2020-12-01.
     * @return bytes with the file
     */
    STHistoricalPoint[] getUsersConnectedHistorical(String startDate, String endDate);

    /**
     * Get a CSV or Excel with statistics related to connected users stored in the platform
     *
     * @param startDate Start date, e.g. 2020-01-01.
     * @param endDate   End date, e.g. 2020-12-01.
     * @param format    Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getUsersConnectedHistoricalExport(String startDate, String endDate, String format);

    /**
     * Get statistics related to the Brokers stored in the platform, such as the total number or their type.
     * The results can be filtered by Environment, UUAA and "Status".
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param platform    Platform (ETHER or NOVA). If it's "ALL", no Platform filtering will be applied.
     * @param status      Filter the results for a specific Status. If it's equals to "ALL", or it's null, or it's empty, no Status filtering is applied.
     * @return A BrokersSummaryDTO.
     */
    BrokersSummaryDTO getBrokersSummary(final String environment, final String uuaa, final String platform, final String status);

    /**
     * Get a CSV or Excel with statistics related to the Brokers stored in the platform, such as the total number or their type.
     * The results can be filtered by Environment, UUAA and "Status".
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param platform    Platform (ETHER or NOVA). If it's "ALL", no Platform filtering will be applied.
     * @param status      Filter the results for a specific Status. If it's equals to "ALL", or it's null, or it's empty, no Status filtering is applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getBrokersSummaryExport(String environment, String uuaa, String platform, String status, String format);

    /**
     * Get the number of brokers for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param status      Status of the broker. If it's 'ALL', no Status filtering will be applied.
     * @param type        Broker Type. If it's 'ALL', no Type filtering will be applied.
     * @param uuaa        Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @param platform    Platform (ETHER or NOVA). If it's "ALL", no Platform filtering will be applied.
     * @return An array of date-value pairs.
     */
    STHistoricalSerie[] getBrokersHistorical(String startDate, String endDate, String environment, String status, String type, String uuaa, String platform);

    /**
     * Get a CSV or Excel with the number of brokers for each day in the given range and for the given filters.
     *
     * @param startDate   Start date, e.g. 2020-01-01.
     * @param endDate     End date, e.g. 2020-12-01.
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @param status      Status of the broker. If it's 'ALL', no Status filtering will be applied.
     * @param type        Broker Type. If it's 'ALL', no Type filtering will be applied.
     * @param uuaa        Product UUAA. If it's 'ALL', no UUAA filtering will be applied.
     * @param platform    Platform (ETHER or NOVA). If it's "ALL", no Platform filtering will be applied.
     * @param format      Format of the file CSV or Excel
     * @return bytes with the file
     */
    byte[] getBrokersHistoricalExport(String startDate, String endDate, String environment, String status, String type, String uuaa, String platform, String format);
}