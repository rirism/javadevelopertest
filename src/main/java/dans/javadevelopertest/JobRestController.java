package dans.javadevelopertest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

@RestController
@RequestMapping("/api")
public class JobRestController {

    @GetMapping("/jobs")
    public ResponseEntity<Object> getJobs() {
        String apiUrl = "http://dev3.dansmultipro.co.id/api/recruitment/positions.json";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Object> response = restTemplate.getForEntity(apiUrl, Object.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return ResponseEntity.ok(response.getBody());
        } else {
            return ResponseEntity.status(response.getStatusCode()).body("Failed to retrieve jobs data");
        }
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<Object> getJobDetail(@PathVariable("id") String jobId) {
        String apiUrl = "http://dev3.dansmultipro.co.id/api/recruitment/positions/" + jobId;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Object> response = restTemplate.getForEntity(apiUrl, Object.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return ResponseEntity.ok(response.getBody());
        } else {
            return ResponseEntity.status(response.getStatusCode()).body("Failed to retrieve job detail");
        }
    }

    @GetMapping(value = "/jobs/download", produces = "text/csv")
    public ResponseEntity<InputStreamResource> downloadJobList() throws IOException {
        String apiUrl = "http://dev3.dansmultipro.co.id/api/recruitment/positions.json";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            String jobListJson = response.getBody();
            String csvData = convertJsonToCsv(jobListJson);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=job_list.csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(new InputStreamResource(inputStream));
        } else {
            return ResponseEntity.status(response.getStatusCode()).body(null);
        }
    }

    private String convertJsonToCsv(String jobListJson) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jobList = objectMapper.readTree(jobListJson);

        CsvMapper csvMapper = new CsvMapper();
        CsvSchema.Builder csvSchemaBuilder = CsvSchema.builder();
        ArrayNode rows = (ArrayNode) jobList;

        if (rows.size() > 0) {
            JsonNode firstRow = rows.get(0);
            firstRow.fieldNames().forEachRemaining(fieldName -> csvSchemaBuilder.addColumn(fieldName));
        }

        CsvSchema csvSchema = csvSchemaBuilder.build().withHeader();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        csvMapper.writer(csvSchema).writeValue(outputStream, rows);

        return outputStream.toString();
    }
}