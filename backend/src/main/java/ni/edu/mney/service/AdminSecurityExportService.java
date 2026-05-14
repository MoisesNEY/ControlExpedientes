package ni.edu.mney.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import ni.edu.mney.service.dto.ManagedUserDTO;
import ni.edu.mney.service.dto.RoleDefinitionDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class AdminSecurityExportService {

    private static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    public ExportedSpreadsheet exportRoles(List<RoleDefinitionDTO> roles) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Roles");
            writeHeader(sheet.createRow(0), "Rol", "Descripción", "Tipo", "Roles compuestos", "Permisos");

            int rowIndex = 1;
            for (RoleDefinitionDTO role : roles) {
                Row row = sheet.createRow(rowIndex++);
                writeCell(row, 0, role.roleName());
                writeCell(row, 1, role.description());
                writeCell(row, 2, role.systemRole() ? "Sistema" : "Dinámico");
                writeCell(row, 3, String.join(", ", role.compositeRoles()));
                writeCell(row, 4, String.join(", ", role.permissions()));
            }

            autoSize(sheet, 5);
            workbook.write(output);
            return new ExportedSpreadsheet("roles-" + DATE_FORMAT.format(LocalDate.now()) + ".xlsx", CONTENT_TYPE, output.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el archivo Excel de roles.", e);
        }
    }

    public ExportedSpreadsheet exportUsers(List<ManagedUserDTO> users) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Usuarios");
            writeHeader(sheet.createRow(0), "Usuario", "Nombres", "Apellidos", "Correo", "Estado", "Roles", "Acciones obligatorias");

            int rowIndex = 1;
            for (ManagedUserDTO user : users) {
                Row row = sheet.createRow(rowIndex++);
                writeCell(row, 0, user.login());
                writeCell(row, 1, user.firstName());
                writeCell(row, 2, user.lastName());
                writeCell(row, 3, user.email());
                writeCell(row, 4, user.activated() ? "Activo" : "Inactivo");
                writeCell(row, 5, String.join(", ", user.roles()));
                writeCell(row, 6, String.join(", ", user.requiredActions()));
            }

            autoSize(sheet, 7);
            workbook.write(output);
            return new ExportedSpreadsheet("usuarios-" + DATE_FORMAT.format(LocalDate.now()) + ".xlsx", CONTENT_TYPE, output.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el archivo Excel de usuarios.", e);
        }
    }

    private void writeHeader(Row row, String... values) {
        for (int index = 0; index < values.length; index++) {
            writeCell(row, index, values[index]);
        }
    }

    private void writeCell(Row row, int index, String value) {
        Cell cell = row.createCell(index);
        cell.setCellValue(value == null ? "" : value);
    }

    private void autoSize(Sheet sheet, int columns) {
        for (int index = 0; index < columns; index++) {
            sheet.autoSizeColumn(index);
        }
    }

    public record ExportedSpreadsheet(String filename, String contentType, byte[] content) {}
}
