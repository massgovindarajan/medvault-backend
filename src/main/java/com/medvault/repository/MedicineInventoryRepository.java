package com.medvault.repository;

import com.medvault.entity.MedicineInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MedicineInventoryRepository extends JpaRepository<MedicineInventory, Long> {

    Optional<MedicineInventory> findByMedicineNameIgnoreCase(String medicineName);

    List<MedicineInventory> findByIsActiveTrueOrderByMedicineNameAsc();

    List<MedicineInventory> findAllByOrderByMedicineNameAsc();
    List<MedicineInventory> findByStockQtyLessThanEqualAndIsActiveTrue(Integer threshold);
}