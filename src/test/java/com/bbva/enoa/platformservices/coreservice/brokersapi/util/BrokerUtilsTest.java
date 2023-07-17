package com.bbva.enoa.platformservices.coreservice.brokersapi.util;

import com.bbva.enoa.apirestgen.brokersapi.model.RateDTO;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemType;
import com.bbva.enoa.datamodel.model.resource.enumerates.HardwarePackType;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Calendar;

import static com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemType.FILESYSTEM;
import static com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemType.FILESYSTEM_ETHER;
import static com.bbva.enoa.datamodel.model.resource.enumerates.HardwarePackType.PACK_ETHER;
import static com.bbva.enoa.datamodel.model.resource.enumerates.HardwarePackType.PACK_NOVA;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.NOT_VALID_PLATFORM_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BrokerUtilsTest
{

    @Nested
    class getFilesystemTypeForPlatform
    {
        @Test
        void ok_NOVA()
        {
            FilesystemType actualFilesystemType = BrokerUtils.getFilesystemTypeForPlatform(Platform.NOVA);
            assertEquals(FILESYSTEM, actualFilesystemType);

        }

        @Test
        void ok_ETHER()
        {
            FilesystemType actualFilesystemType = BrokerUtils.getFilesystemTypeForPlatform(Platform.ETHER);
            assertEquals(FILESYSTEM_ETHER, actualFilesystemType);
        }

        @Test
        void ko_nullPlatform()
        {
            try
            {
                BrokerUtils.getFilesystemTypeForPlatform(null);
            }
            catch (NovaException novaException)
            {
                assertEquals(NOT_VALID_PLATFORM_ERROR, novaException.getErrorCode().getErrorCode());
            }
        }

    }

    @Nested
    class getHardwarePackTypeForPlatform
    {
        @Test
        void ok_NOVA()
        {
            HardwarePackType actual = BrokerUtils.getHardwarePackTypeForPlatform(Platform.NOVA);
            assertEquals(PACK_NOVA, actual);

        }

        @Test
        void ok_ETHER()
        {
            HardwarePackType actual = BrokerUtils.getHardwarePackTypeForPlatform(Platform.ETHER);
            assertEquals(PACK_ETHER, actual);
        }

        @Test
        void ko_nullPlatform()
        {
            try
            {
                BrokerUtils.getHardwarePackTypeForPlatform(null);
            }
            catch (NovaException novaException)
            {
                assertEquals(NOT_VALID_PLATFORM_ERROR, novaException.getErrorCode().getErrorCode());
            }
        }
    }

    @Nested
    class formatCalendar
    {
        @Test
        void ok()
        {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 17);
            cal.set(Calendar.MINUTE, 30);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.MONTH, Calendar.JULY);
            cal.set(Calendar.YEAR, 1992);
            cal.set(Calendar.DATE, 23);

            String actual = BrokerUtils.formatCalendar(cal);
            assertEquals("1992-07-23T17:30:00", actual);
        }

        @Test
        void ok_nullCalendar()
        {
            assertNull(BrokerUtils.formatCalendar(null));
        }
    }

    @Nested
    class getRateInMessagesPerMinute
    {
        @Test
        void rateInSeconds()
        {
            RateDTO rate = new RateDTO();
            rate.setValue(1.0);
            rate.setUnit("s");
            assertEquals(60.0, BrokerUtils.getRateInMessagesPerMinute(rate));
        }

        @Test
        void rateInMinutes()
        {
            RateDTO rate = new RateDTO();
            rate.setValue(23.0);
            rate.setUnit("min");
            assertEquals(23.0, BrokerUtils.getRateInMessagesPerMinute(rate));
        }

        @Test
        void rateInHours()
        {
            RateDTO rate = new RateDTO();
            rate.setValue(120.0);
            rate.setUnit("h");
            assertEquals(2.0, BrokerUtils.getRateInMessagesPerMinute(rate));
        }
    }
}