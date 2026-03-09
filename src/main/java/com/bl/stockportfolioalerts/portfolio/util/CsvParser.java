package com.bl.stockportfolioalerts.portfolio.util;

import com.bl.stockportfolioalerts.portfolio.dto.HoldingRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvParser {

    public static List<HoldingRequest> parse(MultipartFile file) {

        List<HoldingRequest> holdings = new ArrayList<>();

        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] data = line.split(",");

                HoldingRequest h = new HoldingRequest();
                h.setTicker(data[0]);
                h.setQuantity(Integer.parseInt(data[1]));
                h.setBuyingPrice(Double.parseDouble(data[2]));

                holdings.add(h);
            }

        } catch (Exception e) {
            throw new RuntimeException("Invalid CSV");
        }

        return holdings;
    }
}