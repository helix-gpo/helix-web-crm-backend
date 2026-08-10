package com.helix.gpo.web_crm.website.internal;

import com.helix.gpo.web_crm.project.ProjectApi;
import com.helix.gpo.web_crm.project.PublicProjectSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
class WebsiteProjectController {

    private final ProjectApi projectApi;

    @GetMapping("/api/public/projects")
    List<PublicProjectSummary> findVisibleProjects() {
        return projectApi.findAllVisibleOnWebsite();
    }

}
