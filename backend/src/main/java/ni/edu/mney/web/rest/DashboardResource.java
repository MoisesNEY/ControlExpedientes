package ni.edu.mney.web.rest;

import ni.edu.mney.security.AuthoritiesConstants;
import ni.edu.mney.service.DashboardService;
import ni.edu.mney.service.dto.DashboardMetricsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardResource {

    private final DashboardService dashboardService;

    public DashboardResource(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "')")
    public ResponseEntity<DashboardMetricsDTO> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminMetrics());
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MEDICO + "')")
    public ResponseEntity<DashboardMetricsDTO> getDoctorDashboard() {
        return ResponseEntity.ok(dashboardService.getDoctorMetrics());
    }

    @GetMapping("/nurse")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.ENFERMERO + "')")
    public ResponseEntity<DashboardMetricsDTO> getNurseDashboard() {
        return ResponseEntity.ok(dashboardService.getNurseMetrics());
    }

    @GetMapping("/reception")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.RECEPCION + "')")
    public ResponseEntity<DashboardMetricsDTO> getReceptionDashboard() {
        return ResponseEntity.ok(dashboardService.getReceptionMetrics());
    }
}