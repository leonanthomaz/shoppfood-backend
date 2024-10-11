package com.microshoppe.erp.delivery.service;

import com.microshoppe.erp.delivery.model.Delivery;
import com.microshoppe.erp.delivery.model.DeliveryZone;
import com.microshoppe.erp.delivery.repository.DeliveryRepository;
import com.microshoppe.erp.delivery.repository.DeliveryZoneRepository;
import com.microshoppe.erp.delivery.dto.DeliveryMapDTO;
import com.microshoppe.erp.store.model.Store;
import com.microshoppe.erp.store.repository.StoreRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@AllArgsConstructor
public class DeliveryService {

    private final StoreRepository storeRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryZoneRepository deliveryZoneRepository;

    public void createDeliveryMap(DeliveryMapDTO data, String merchantCode) {
        // Buscar a loja pelo ID
        Store store = storeRepository.findByMerchantCode(merchantCode);

        // Criar a entidade Delivery com o ponto central e o raio
        Delivery delivery = Delivery.builder()
                .merchantCode(merchantCode)
                .cep(data.getCep())
                .defaultDeliveryFee(data.getDefaultDeliveryFee())
                .centralPointLat(data.getCentralPoint().getLat())
                .centralPointLng(data.getCentralPoint().getLng())
                .radius(data.getRadius())
                .store(store)
                .build();

        // Processar os bairros e gravar como DeliveryZone, associando cada um ao Delivery
        List<DeliveryZone> deliveryZones = data.getNeighborhoods().stream().map(neighborhood -> {
            return DeliveryZone.builder()
                    .merchantCode(merchantCode)
                    .name(neighborhood.getName())
                    .price(neighborhood.getPrice())
                    .lat(neighborhood.getLat())
                    .lng(neighborhood.getLng())
                    .delivery(delivery) // Aqui associamos a zona ao Delivery
                    .build();
        }).collect(Collectors.toList());

        delivery.setZones(deliveryZones);

        // Salvar a entidade Delivery primeiro
        deliveryRepository.save(delivery);

        // Agora, podemos salvar as DeliveryZones, que já estão associadas ao Delivery
        deliveryZoneRepository.saveAll(deliveryZones);
    }

    public Delivery findByMerchantCode(String merchantCode) {
        log.info("DELIVERY SERVICE >>> DADOS ENCONTRADOS : {}", deliveryRepository.findByMerchantCode(merchantCode));
        return deliveryRepository.findByMerchantCode(merchantCode);
    }
}
