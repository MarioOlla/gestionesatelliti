package it.prova.gestionesatelliti.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import it.prova.gestionesatelliti.model.Satellite;

public interface SatelliteRepository extends CrudRepository<Satellite, Long>, JpaSpecificationExecutor<Satellite> {
	
	@Query("from Satellite where dataLancio < :twoYearsAgo")
	List<Satellite> findAllLaunchedMoreThan2YearsAgo(Date twoYearsAgo);
	
	@Query("from Satellite where dataRientro=null and stato='DISATTIVATO'")
	List<Satellite> findDisattivatiMaInOrbita();

	@Query("from Satellite where dataLancio < :menoDieciAnni and dataRientro=null and stato='FISSO'")
	List<Satellite> findFissiInOrbitaDa10Anni(Date menoDieciAnni);

	@Query("from Satellite where not stato='DISATTIVATO' and (dataRientro=null or dataRientro>curdate())")
	List<Satellite> findAllNotDisattivatiAndNotReturned();
}
