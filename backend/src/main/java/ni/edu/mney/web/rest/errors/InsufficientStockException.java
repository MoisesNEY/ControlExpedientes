package ni.edu.mney.web.rest.errors;

import java.net.URI;

public class InsufficientStockException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    public InsufficientStockException(String medicineName) {
        super(URI.create(ErrorConstants.PROBLEM_BASE_URL + "/insufficient-stock"),
                "Stock insuficiente para el medicamento: " + medicineName,
                "Medicamento",
                "insufficientstock");
    }
}
