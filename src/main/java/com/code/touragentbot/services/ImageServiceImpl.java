package com.code.touragentbot.services;

import com.code.touragentbot.models.Offer;
import lombok.NonNull;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

@Service
public class ImageServiceImpl implements ImageService {

    @Override
    public File convertOfferToImage(Offer offer) throws FileNotFoundException, JRException {
        File file = ResourceUtils.getFile("classpath:offer.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
        List<Offer> offers = new ArrayList<>();
        offers.add(offer);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(offers);
        Map<String, Object> parameters = new HashMap<>();
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        try{
            File out = new File(offer.getRequestId() + offer.getUserEmail() + "." + "jpg");
            BufferedImage image = (BufferedImage) JasperPrintManager.printPageToImage(jasperPrint, 0,1f);
            ImageIO.write(image, "jpg", out);
            return out;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}