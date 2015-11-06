/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.admin.services;

import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Ehcache;

import org.kaaproject.kaa.common.dto.admin.RecordKey;
import org.kaaproject.kaa.common.dto.admin.SdkPropertiesDto;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.server.admin.services.cache.CacheService;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.control.service.ControlService;
import org.kaaproject.kaa.server.control.service.exception.ControlServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CacheServiceImpl implements CacheService {
    
    private static final String SDK_CACHE = "sdkCache";
    private static final String RECORD_LIBRARY_CACHE = "recordLibraryCache";
    private static final String RECORD_SCHEMA_CACHE = "recordSchemaCache";
    private static final String RECORD_DATA_CACHE = "recordDataCache";
    private static final String FILE_UPLOAD_CACHE = "fileUploadCache";

    @Autowired
    private ControlService controlService;

    @Autowired
    private CacheManager adminCacheManager;

    @Override
    @Cacheable(value = SDK_CACHE, key = "#key", unless="#result == null")
    public FileData getSdk(SdkPropertiesDto key) {
        return null;
    }
    
    @Override
    @CachePut(value = SDK_CACHE, key = "#key")
    public FileData putSdk(SdkPropertiesDto key, FileData sdkFile) {
        return sdkFile;
    }

    @Override
    @CacheEvict(value = SDK_CACHE, key = "#key")
    public void flushSdk(SdkPropertiesDto key) {
    }
    
    public List<SdkPropertiesDto> getCachedSdkKeys(String applicationId) {
        List<SdkPropertiesDto> keys = new ArrayList<>();
        Ehcache cache = (Ehcache) adminCacheManager.getCache(SDK_CACHE).getNativeCache();
        List<?> cachedKeys = cache.getKeysWithExpiryCheck();
        for (Object cachedKey : cachedKeys) {
            if (cachedKey instanceof SdkPropertiesDto) {
                SdkPropertiesDto cachedSdkPropertiesDto = (SdkPropertiesDto)cachedKey;
                if (applicationId.equals(cachedSdkPropertiesDto.getApplicationId())) {
                    keys.add(cachedSdkPropertiesDto);
                }
            }
        }
        return keys;
    }

    @Override
    @Cacheable(RECORD_LIBRARY_CACHE)
    public FileData getRecordLibrary(RecordKey key) throws KaaAdminServiceException {
        try {
            return controlService.generateRecordStructureLibrary(key.getApplicationId(), key.getSchemaVersion());
        } catch (ControlServiceException e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    @Cacheable(RECORD_SCHEMA_CACHE)
    public FileData getRecordSchema(RecordKey key) throws KaaAdminServiceException {
        try {
            return controlService.getRecordStructureSchema(key.getApplicationId(), key.getSchemaVersion());
        } catch (ControlServiceException e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    @Cacheable(RECORD_DATA_CACHE)
    public FileData getRecordData(RecordKey key) throws KaaAdminServiceException {
        try {
            return controlService.getRecordStructureData(key);
        } catch (ControlServiceException e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    @Cacheable(value = FILE_UPLOAD_CACHE, key = "#key")
    public byte[] uploadedFile(String key, byte[] data) {
        return data;
    }

    @Override
    @CacheEvict(value = FILE_UPLOAD_CACHE, key = "#key")
    public void removeUploadedFile(String key) {
    }

}