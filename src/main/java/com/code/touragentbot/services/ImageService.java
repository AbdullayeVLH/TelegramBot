package com.code.touragentbot.services;

import com.code.touragentbot.models.Offer;
import lombok.NonNull;
import net.sf.jasperreports.engine.JRException;

import java.io.File;
import java.io.FileNotFoundException;

public interface ImageService {
    @NonNull File convertOfferToImage(Offer offer) throws FileNotFoundException, JRException;

}
