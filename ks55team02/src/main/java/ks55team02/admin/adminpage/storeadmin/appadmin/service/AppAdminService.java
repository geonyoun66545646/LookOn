package ks55team02.admin.adminpage.storeadmin.appadmin.service;

import java.util.List;

import ks55team02.admin.common.domain.SearchCriteria;
import ks55team02.common.domain.store.AppStore;

public interface AppAdminService {

    int getAppAdminCount(SearchCriteria searchCriteria);

    List<AppStore> getAppAdminList(SearchCriteria searchCriteria, int limitStart, int pageSize);

    AppStore getAppAdminById(String aplyId);

    void processApplicationStatus(String aplyId, String statusToUpdate, String reason);
}