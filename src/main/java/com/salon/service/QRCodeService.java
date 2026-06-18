package com.salon.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.salon.model.Booking;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class QRCodeService {

    public byte[] generateBookingQRCode(Booking booking) throws Exception {
        String qrData = String.format(
                "Booking ID: %d\nCustomer: %s\nDate: %s\nTime: %s\nService: %s\nStaff: %s",
                booking.getId(),
                booking.getUser().getFullName(),
                booking.getDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                booking.getTime().format(DateTimeFormatter.ofPattern("hh:mm a")),
                booking.getService() != null ? booking.getService().getServiceName() : "N/A",
                booking.getStaff() != null ? booking.getStaff().getStaffName() : "TBD"
        );

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
        return baos.toByteArray();
    }
}