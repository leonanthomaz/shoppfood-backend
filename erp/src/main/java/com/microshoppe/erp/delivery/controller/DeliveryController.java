package com.microshoppe.erp.delivery.controller;

import com.microshoppe.erp.delivery.model.Delivery;
import com.microshoppe.erp.delivery.service.DeliveryService;
import com.microshoppe.erp.delivery.dto.DeliveryMapDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/create-delivery-map/{merchantCode}")
    public ResponseEntity<String> createStoreMap(@PathVariable String merchantCode, @RequestBody @Valid DeliveryMapDTO data) {
        try {
            log.info("DELIVERY >>> CRIAÇÃO DE MAPA >>> DADOS RECEBIDOS: {}", data);

            // Processar o ponto central e o raio
            log.info("DELIVERY >>> PONTO CENTRAL: lat={}, lng={}", data.getCentralPoint().getLat(), data.getCentralPoint().getLng());
            log.info("DELIVERY >>> RAIO DE ENTREGA: {}km", data.getRadius());

            // Processar os bairros com Stream e log
            data.getNeighborhoods().stream()
                    .forEach(bairro -> log.info("DELIVERY >>> BAIRRO: {}, Preço: {}, Latitude: {}, Longitude: {}",
                            bairro.getName(), bairro.getPrice(), bairro.getLat(), bairro.getLng()));

            // Chama o serviço para processar e salvar no banco
            deliveryService.createDeliveryMap(data, merchantCode);

            return ResponseEntity.status(HttpStatus.CREATED).body("Mapa de entrega criado com sucesso");
        } catch (Exception e) {
            log.error("DELIVERY >>> FALHA AO CRIAR MAPA DE ENTREGA: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao criar mapa de entrega");
        }
    }

    @GetMapping("/check/{merchantCode}")
    public ResponseEntity<Delivery> getDeliveryInfo(@PathVariable String merchantCode) {
        log.info("ROTA DELIVERY >>> DADOS RECEBIDOS >>> {}", merchantCode);
        try {
            Delivery delivery = deliveryService.findByMerchantCode(merchantCode);
            log.info("ROTA DELIVERY >>> DADOS ENCONTRADOS >>> {}", delivery);
            if (delivery == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.OK).body(delivery);
        } catch (RuntimeException error){
            throw new RuntimeException("ROTA DELIVERY >>> FALHA AO RECUPERAR DELIVERY: ", error);
        }
    }
}
