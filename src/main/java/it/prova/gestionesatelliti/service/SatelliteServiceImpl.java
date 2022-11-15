package it.prova.gestionesatelliti.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.prova.gestionesatelliti.model.Satellite;
import it.prova.gestionesatelliti.repository.SatelliteRepository;

@Service
public class SatelliteServiceImpl implements SatelliteService {
	
	@Autowired
	private SatelliteRepository satelliteRepository;

	@Override
	@Transactional(readOnly = true)
	public List<Satellite> listAllElements() {
		return (List<Satellite>) satelliteRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Satellite caricaSingoloElemento(Long id) {
		return satelliteRepository.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public void aggiorna(Satellite satelliteInstance) {
		satelliteRepository.save(satelliteInstance);
		
	}

	@Override
	@Transactional
	public void inserisciNuovo(Satellite satelliteInstance) {
		satelliteRepository.save(satelliteInstance);
	}

	@Override
	@Transactional
	public void rimuovi(Long idSatellite) {
		satelliteRepository.deleteById(idSatellite);
		
	}

	@Override
	public List<Satellite> findByExample(Satellite example) {
		Specification<Satellite> criteriSpecificazione = (root,query,cb) -> {
			
			List<Predicate> predicates = new ArrayList<Predicate>();
			
			if (StringUtils.isNotEmpty(example.getDenominazione()))
				predicates.add(cb.like(cb.upper(root.get("nome")), "%" + example.getDenominazione().toUpperCase() + "%"));

			if (StringUtils.isNotEmpty(example.getCodice()))
				predicates.add(cb.like(cb.upper(root.get("codice")), "%" + example.getCodice().toUpperCase() + "%"));
			
			if (example.getDataLancio() != null)
				predicates.add(cb.greaterThanOrEqualTo(root.get("dataLancio"), example.getDataLancio()));
			
			if (example.getDataRientro() != null)
				predicates.add(cb.greaterThanOrEqualTo(root.get("dataRientro"), example.getDataRientro()));
				
			if (example.getStato() != null)
				predicates.add(cb.equal(root.get("stato"), example.getStato()));

			

			return cb.and(predicates.toArray(new Predicate[predicates.size()]));
		};

		return satelliteRepository.findAll(criteriSpecificazione);
	}
	
	@Transactional(readOnly = true)
	public List<Satellite> tuttiLanciatiPiuDiDueAnniFa(){
		Date date = new Date();
        // Conversione da Date a Calendar
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        // -2 anni
        c.add(Calendar.YEAR, -2);
        // Riconversione a data
        Date menoDueAnni = c.getTime();
		return satelliteRepository.findAllLaunchedMoreThan2YearsAgo(menoDueAnni);
	}
	
	@Transactional(readOnly = true)
	public List<Satellite> trovaTuttiDisattivatiMaInOrbita(){
		return satelliteRepository.findDisattivatiMaInOrbita();
	}

	@Transactional(readOnly = true)
	public List<Satellite> trovaTuttiFissiInOrbitaDaDieciAnni() {
		Date date = new Date();
        // Conversione da Date a Calendar
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        // -2 anni
        c.add(Calendar.YEAR, -10);
        // Riconversione a data
        Date menoDieciAnni = c.getTime();
		return satelliteRepository.findFissiInOrbitaDa10Anni(menoDieciAnni);
	}
}
