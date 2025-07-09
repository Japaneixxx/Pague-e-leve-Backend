package com.japaneixxx.pagueleve.util;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Classe utilitária para gerar o payload do BR Code PIX (Copia e Cola).
 * Segue a especificação do BACEN para QR Codes estáticos.
 */
public class PixGenerator {

    // Constantes dos campos do BR Code
    private static final String ID_PAYLOAD_FORMAT_INDICATOR = "00";
    private static final String ID_MERCHANT_ACCOUNT_INFORMATION = "26";
    private static final String ID_MERCHANT_ACCOUNT_INFORMATION_GUI = "00";
    private static final String ID_MERCHANT_ACCOUNT_INFORMATION_KEY = "01";
    private static final String ID_MERCHANT_CATEGORY_CODE = "52";
    private static final String ID_TRANSACTION_CURRENCY = "53";
    private static final String ID_TRANSACTION_AMOUNT = "54";
    private static final String ID_COUNTRY_CODE = "58";
    private static final String ID_MERCHANT_NAME = "59";
    private static final String ID_MERCHANT_CITY = "60";
    private static final String ID_ADDITIONAL_DATA_FIELD_TEMPLATE = "62";
    private static final String ID_ADDITIONAL_DATA_FIELD_TEMPLATE_TXID = "05";
    private static final String ID_CRC16 = "63";

    public static String generatePayload(String pixKey, BigDecimal amount, String merchantName, String merchantCity, String txid) {
        // --- MUDANÇA CRÍTICA AQUI ---
        // Limpa a chave PIX para remover formatações (pontos, traços, etc.)
        String cleanedPixKey = cleanPixKey(pixKey);

        // Garante que os campos não excedam o limite de caracteres do padrão PIX.
        if (merchantName.length() > 25) {
            merchantName = merchantName.substring(0, 25);
        }
        if (merchantCity.length() > 15) {
            merchantCity = merchantCity.substring(0, 15);
        }
        if (txid.length() > 25) {
            txid = txid.substring(0, 25);
        }

        // Normaliza os nomes para remover acentos e caracteres especiais
        merchantName = normalizeString(merchantName);
        merchantCity = normalizeString(merchantCity);

        // Formata o valor da transação
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("0.00", symbols);
        String formattedAmount = df.format(amount);

        // Monta os campos do payload
        String payload = formatField(ID_PAYLOAD_FORMAT_INDICATOR, "01") +
                buildMerchantAccountInformation(cleanedPixKey) + // Usa a chave limpa
                formatField(ID_MERCHANT_CATEGORY_CODE, "0000") +
                formatField(ID_TRANSACTION_CURRENCY, "986") + // 986 = BRL
                formatField(ID_TRANSACTION_AMOUNT, formattedAmount) +
                formatField(ID_COUNTRY_CODE, "BR") +
                formatField(ID_MERCHANT_NAME, merchantName) +
                formatField(ID_MERCHANT_CITY, merchantCity) +
                buildAdditionalData(txid);

        // Calcula e adiciona o CRC16
        String payloadWithCrc = payload + ID_CRC16 + "04";
        String crc = calculateCRC16(payloadWithCrc);

        return payloadWithCrc + crc;
    }

    private static String buildMerchantAccountInformation(String pixKey) {
        String gui = formatField(ID_MERCHANT_ACCOUNT_INFORMATION_GUI, "br.gov.bcb.pix");
        String key = formatField(ID_MERCHANT_ACCOUNT_INFORMATION_KEY, pixKey);
        return formatField(ID_MERCHANT_ACCOUNT_INFORMATION, gui + key);
    }

    private static String buildAdditionalData(String txid) {
        String transactionId = formatField(ID_ADDITIONAL_DATA_FIELD_TEMPLATE_TXID, txid);
        return formatField(ID_ADDITIONAL_DATA_FIELD_TEMPLATE, transactionId);
    }

    private static String formatField(String id, String value) {
        String length = String.format("%02d", value.length());
        return id + length + value;
    }

    private static String normalizeString(String str) {
        if (str == null) return "";
        String normalized = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD);
        normalized = normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return normalized.toUpperCase();
    }

    /**
     * NOVO MÉTODO: Remove caracteres de formatação comuns de chaves PIX.
     * Isso torna o gerador robusto a erros de digitação no cadastro da loja.
     */
    private static String cleanPixKey(String pixKey) {
        if (pixKey == null) {
            return "";
        }
        // Remove pontos, traços, barras, parênteses e espaços.
        return pixKey.replaceAll("[.\\-/()\\s]", "");
    }

    private static String calculateCRC16(String data) {
        int crc = 0xFFFF;
        for (byte b : data.getBytes(StandardCharsets.UTF_8)) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc <<= 1;
                }
            }
        }
        return String.format("%04X", crc & 0xFFFF);
    }
}