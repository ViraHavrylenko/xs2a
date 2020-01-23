/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.aspsp.profile.mapper;

import de.adorsys.psd2.aspsp.profile.config.BankProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AspspSettingsToBankProfileSettingMapperImpl.class})
 class AspspSettingsToBankProfileSettingMapperTest {

    @Autowired
    private AspspSettingsToBankProfileSettingMapper mapper;

    private JsonReader jsonReader = new JsonReader();
    private BankProfileSetting initialBankProfileSetting;
    private BankProfileSetting expectedBankProfileSetting;
    private AspspSettings aspspSettings;

    @BeforeEach
     void setUp() {
        initialBankProfileSetting = jsonReader.getObjectFromFile("json/mapper/initial-bank-profile-setting.json", BankProfileSetting.class);
        expectedBankProfileSetting = jsonReader.getObjectFromFile("json/mapper/expected-bank-profile-setting.json", BankProfileSetting.class);
        aspspSettings = jsonReader.getObjectFromFile("json/mapper/aspsp-settings.json", AspspSettings.class);
    }

    @Test
     void updateBankProfileSetting_success() {
        mapper.updateBankProfileSetting(aspspSettings, initialBankProfileSetting);
        assertEquals(expectedBankProfileSetting, initialBankProfileSetting);
    }
}
