package ks55team02.admin.adminpage.storeadmin.appadmin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ks55team02.admin.adminpage.storeadmin.appadmin.domain.StatusUpdateRequest;
import ks55team02.admin.adminpage.storeadmin.appadmin.service.AppAdminService;
import ks55team02.admin.common.domain.Pagination;
import ks55team02.admin.common.domain.SearchCriteria;
import ks55team02.common.domain.store.AppStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/adminpage/storeadmin")
@RequiredArgsConstructor
@Slf4j
public class AppAdminController {

    private final AppAdminService appAdminService;

    @GetMapping("/appAdmin")
    public String appAdmin(Model model, @ModelAttribute(name = "searchCriteria") SearchCriteria searchCriteria) {
        int totalCount = appAdminService.getAppAdminCount(searchCriteria);
        Pagination pagination = new Pagination(totalCount, searchCriteria);
        List<AppStore> appAdminList = appAdminService.getAppAdminList(searchCriteria, pagination.getLimitStart(), pagination.getRecordSize());

        model.addAttribute("title", "상점 신청 관리");
        model.addAttribute("appAdminList", appAdminList);
        model.addAttribute("pagination", pagination);
        return "admin/adminpage/storeadmin/appAdminView";
    }

    @GetMapping("/appDetail")
    public String appDetail(@RequestParam(name = "aplyId") String aplyId, Model model) {
        AppStore appStore = appAdminService.getAppAdminById(aplyId);
        model.addAttribute("title", "상점 신청 상세");
        model.addAttribute("appStore", appStore);
        return "admin/adminpage/storeadmin/appDetailView";
    }

    @PostMapping("/updateStatus")
    @ResponseBody
    public Map<String, Object> updateStatus(@RequestBody AppStore appStore) {
        Map<String, Object> response = new HashMap<>();
        try {
            appAdminService.processApplicationStatus(appStore.getAplyId(), appStore.getAplyStts(), appStore.getAprvRjctRsn());
            response.put("status", "success");
            response.put("message", "상태가 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            log.error("단일 상태 업데이트 오류: aplyId={}", appStore.getAplyId(), e);
            response.put("status", "error");
            response.put("message", "상태 업데이트 중 오류 발생: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/updateMultipleStatus")
    @ResponseBody
    public Map<String, Object> updateMultipleStatus(@RequestBody StatusUpdateRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            for (String aplyId : request.getAplyIds()) {
                 appAdminService.processApplicationStatus(aplyId, request.getNewStatus(), request.getRejectionReason());
            }
            response.put("status", "success");
            response.put("message", request.getAplyIds().size() + "건의 신청이 성공적으로 처리되었습니다.");
        } catch (Exception e) {
            log.error("일괄 상태 업데이트 오류", e);
            response.put("status", "error");
            response.put("message", "일괄 처리 중 오류 발생: " + e.getMessage());
        }
        return response;
    }
}