package com.q4magic.opportunityProducts.serviceImpl;

import com.q4magic.common.dto.OpportunityProductsDto;
import com.q4magic.common.dto.SyncRecordsQueueDto;
import com.q4magic.common.models.Opportunities;
import com.q4magic.common.models.OpportunityProducts;
import com.q4magic.common.models.Products;
import com.q4magic.common.models.SyncRecordsQueue;
import com.q4magic.common.repository.OpportunitiesRepository;
import com.q4magic.common.repository.OpportunityProductsRepository;
import com.q4magic.common.repository.ProductsRepository;
import com.q4magic.common.repository.SyncRecordsQueueRepository;
import com.q4magic.common.service.CommonService;
import com.q4magic.opportunityProducts.service.OpportunityProductsService;
import com.q4magic.syncRecordsQueue.service.SyncRecordsQueueService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service(value = "OpportunityProductsService")
public class OpportunityProductsServiceImpl implements OpportunityProductsService {

    @Autowired
    private OpportunitiesRepository opportunitiesRepository;

    @Autowired
    private OpportunityProductsRepository opportunityProductsRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private SyncRecordsQueueService syncRecordsQueueService;

    @Autowired
    private SyncRecordsQueueRepository syncRecordsQueueRepository;

    @Override
    public List<OpportunityProductsDto> getAllOppProducts(Integer id) {
        try {
            List<OpportunityProducts> opportunityProductsList = this.opportunityProductsRepository.getByOppId(id);
            List<OpportunityProductsDto> opportunityProductsDtoList = new ArrayList<>();
            if (!opportunityProductsList.isEmpty()) {
                for (OpportunityProducts opportunityProducts : opportunityProductsList) {
                    opportunityProductsDtoList.add(this.getOppProducts(opportunityProducts.getId()));
                }
            }
            return opportunityProductsDtoList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public OpportunityProductsDto getOppProducts(Integer id) {
        try {
            OpportunityProducts opportunityProducts = this.opportunityProductsRepository.findById(id).orElseThrow(() -> new RuntimeException("Opp products not found"));
            OpportunityProductsDto opportunityProductsDto = new OpportunityProductsDto();
            opportunityProductsDto.setOppId(opportunityProducts.getOpportunities().getId());
            opportunityProductsDto.setProductId(opportunityProducts.getProducts().getId());
            BeanUtils.copyProperties(opportunityProducts, opportunityProductsDto);
            return opportunityProductsDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public OpportunityProductsDto createOppProducts(OpportunityProductsDto opportunityProductsDto, Boolean isSyncToSalesforce, Integer createdById) {
        try {
            OpportunityProducts opportunityProducts = new OpportunityProducts();
            opportunityProducts.setIsDeleted(false);

            Opportunities opportunities = this.opportunitiesRepository.findById(opportunityProductsDto.getOppId()).orElseThrow(() -> new RuntimeException("Opp not found"));
            opportunityProducts.setOpportunities(opportunities);

            Products products = this.productsRepository.findById(opportunityProductsDto.getProductId()).orElseThrow(() -> new RuntimeException("Product not found"));
            opportunityProducts.setProducts(products);

            if (opportunityProductsDto.getCreatedDate() != null) {
                opportunityProducts.setCreatedDate(this.commonService.convertStringToDate(opportunityProductsDto.getCreatedDate()));
            } else {
                opportunityProducts.setCreatedDate(new Date());
            }

            BeanUtils.copyProperties(opportunityProductsDto, opportunityProducts, "createdDate", "isDeleted");
            this.opportunityProductsRepository.save(opportunityProducts);
            if (isSyncToSalesforce) {
                SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                syncRecordsQueueDto.setSubjectId(opportunityProducts.getId());
                syncRecordsQueueDto.setSubject("OpportunitiesProducts");
                syncRecordsQueueDto.setOperationType("CREATE");
                syncRecordsQueueDto.setSyncType("PUSH");
                syncRecordsQueueDto.setDeleted(false);
                syncRecordsQueueDto.setCreatedBy(createdById);
                this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
            }
            return opportunityProductsDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public OpportunityProductsDto updateOppProducts(Integer id, OpportunityProductsDto opportunityProductsDto, Boolean isSyncToSalesforce, Integer createdById) {
        try {
            OpportunityProducts opportunityProducts = this.opportunityProductsRepository.findById(id).orElseThrow(() -> new RuntimeException("Opp products not found"));
            Products products = this.productsRepository.findById(opportunityProductsDto.getProductId()).orElseThrow(() -> new RuntimeException("Product not found"));
            opportunityProducts.setProducts(products);
            opportunityProducts.setIsDeleted(false);
            BeanUtils.copyProperties(opportunityProductsDto, opportunityProducts, "id", "opportunities", "products", "createdDate", "isDeleted");
            this.opportunityProductsRepository.save(opportunityProducts);
            if (isSyncToSalesforce && opportunityProducts.getOpportunityProductId() != null) {
                SyncRecordsQueue syncRecordsQueue = this.syncRecordsQueueRepository.findBySubjectId(id, createdById);
                if (syncRecordsQueue == null) {
                    SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                    syncRecordsQueueDto.setSubjectId(id);
                    syncRecordsQueueDto.setSubject("OpportunitiesProducts");
                    syncRecordsQueueDto.setOperationType("UPDATE");
                    syncRecordsQueueDto.setSyncType("PUSH");
                    syncRecordsQueueDto.setDeleted(false);
                    syncRecordsQueueDto.setCreatedBy(createdById);
                    this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
                }
            }
            return opportunityProductsDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteOppProducts(Integer id, Boolean isSyncToSalesforce, Integer createdById) {
        try {
            OpportunityProducts opportunityProducts = this.opportunityProductsRepository.findById(id).orElseThrow(() -> new RuntimeException("Opp products not found"));
            if (isSyncToSalesforce && opportunityProducts.getOpportunityProductId() != null) {
                opportunityProducts.setIsDeleted(true);
                this.opportunityProductsRepository.save(opportunityProducts);
                SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                syncRecordsQueueDto.setSubjectId(opportunityProducts.getId());
                syncRecordsQueueDto.setSubject("OpportunitiesProducts");
                syncRecordsQueueDto.setOperationType("DELETE");
                syncRecordsQueueDto.setSyncType("PUSH");
                syncRecordsQueueDto.setDeleted(false);
                syncRecordsQueueDto.setCreatedBy(createdById);
                this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
            } else {
                this.opportunityProductsRepository.delete(opportunityProducts);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
