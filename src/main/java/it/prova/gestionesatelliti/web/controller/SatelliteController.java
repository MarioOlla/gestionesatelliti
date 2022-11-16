package it.prova.gestionesatelliti.web.controller;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.prova.gestionesatelliti.model.Satellite;
import it.prova.gestionesatelliti.model.StatoSatellite;
import it.prova.gestionesatelliti.service.SatelliteService;

@Controller
@RequestMapping(value = "/satellite")
public class SatelliteController {

	@Autowired
	private SatelliteService satelliteService;

	@GetMapping
	public ModelAndView listAll() {
		ModelAndView mv = new ModelAndView();
		List<Satellite> results = satelliteService.listAllElements();
		mv.addObject("satellite_list_attribute", results);
		mv.setViewName("satellite/list");
		return mv;
	}

	@GetMapping("/search")
	public String search() {
		return "satellite/search";
	}

	@PostMapping("/list")
	public String listByExample(Satellite example, ModelMap model) {
		List<Satellite> results = satelliteService.findByExample(example);
		model.addAttribute("satellite_list_attribute", results);
		return "satellite/list";
	}

	@GetMapping("/insert")
	public String create(Model model) {
		model.addAttribute("insert_satellite_attr", new Satellite());
		return "satellite/insert";
	}

	@PostMapping("/save")
	public String save(@Valid @ModelAttribute("insert_satellite_attr") Satellite satellite, BindingResult result,
			RedirectAttributes redirectAttrs) {
		Date now = new Date();
		// se ci sono errori di validazione...
		if (result.hasErrors())
			return "satellite/insert"; // torno alla insert
		String validazione = validaParametri(satellite, now, result);
		if (validazione != null)
			return validazione+"insert";
		// altrimenti Inserisco
		satelliteService.inserisciNuovo(satellite);

		redirectAttrs.addFlashAttribute("successMessage", "Operazione eseguita correttamente");
		return "redirect:/satellite";
	}

	@GetMapping("/show/{idSatellite}")
	public String show(@PathVariable(required = true) Long idSatellite, Model model) {
		model.addAttribute("show_satellite_attr", satelliteService.caricaSingoloElemento(idSatellite));
		return "satellite/show";
	}

	@GetMapping("/preDelete/{idSatellite}")
	public String prepareDelete(@PathVariable(required = true) Long idSatellite, Model model) {
		model.addAttribute("toDelete_satellite_attr", satelliteService.caricaSingoloElemento(idSatellite));
		return "satellite/delete";
	}

	@PostMapping("/delete")
	public String delete(@RequestParam(required = true) Long idSatellite, RedirectAttributes redirectAttrs) {

		Satellite s = satelliteService.caricaSingoloElemento(idSatellite);

		if ((s.getDataLancio() != null && s.getDataRientro() == null))
			return "redirect:/satellite";
		if (s.getDataRientro() != null
				&& (s.getDataRientro().after(new Date()) || s.getDataLancio().before(new Date())))
			return "redirect:/satellite";

		satelliteService.rimuovi(idSatellite);
		redirectAttrs.addFlashAttribute("successMessage", "Operazione eseguita correttamente");
		return "redirect:/satellite";
	}

	@GetMapping("/preUpdate/{idSatellite}")
	public String prepareUpdate(@PathVariable(required = true) Long idSatellite, Model model) {
		model.addAttribute("toUpdate_satellite_attr", satelliteService.caricaSingoloElemento(idSatellite));
		return "satellite/update";
	}

	@PostMapping("/update")
	public String update(@Valid @ModelAttribute("toUpdate_satellite_attr") Satellite satellite, BindingResult result,
			RedirectAttributes redirectAttrs) {

		Date now = new Date();
		Satellite s = satelliteService.caricaSingoloElemento(satellite.getId());
		System.out.println("sono entrato nell'update");
		if (s.getDataRientro() != null && dopoIlRientro(satellite, now)) {
			
		} else {
			if (result.hasErrors())
				return "satellite/update";
			if (satellite.getDataRientro()==null || primaDelRientro(satellite, now)) {
				if(satellite.getDataLancio()!=null && dopoIlLancio(satellite, now)) {
					if(satellite.getDataLancio().after(s.getDataLancio())) {
						result.rejectValue("dataLancio", "satellite.dataLancio.illegalDateEdit");
						return "satellite/update";
					}
					if(satellite.getStato()==null && dopoIlLancio(s, now)) {
						result.rejectValue("stato", "satellite.stato.stateShouldExist");
						return "satellite/update";
					}	
				}else {
					String validazione = validaParametri(satellite, now, result);
					if(validazione != null)
						return validazione+"update";
				}
			}else {
				redirectAttrs.addFlashAttribute("errorMessage",
						"Operazione fallita, impossibile modificare un satellite già rientrato.");
				return "redirect:/satellite";
			}
		}
		
		satelliteService.aggiorna(satellite);
		redirectAttrs.addFlashAttribute("successMessage", "Operazione eseguita correttamente");
		return "redirect:/satellite";
	}
	
	@GetMapping("/preDisabilita")
	public ModelAndView preparaDisabilitazione() {
		ModelAndView mv = new ModelAndView();
		mv.addObject("quanti_rientrano", satelliteService.tuttiINonDisattivatiNonAncoraRientrati().size());
		mv.addObject("quanti_su_db", satelliteService.listAllElements().size());
		mv.setViewName("satellite/disabilita");
		return mv;
	}
	
	@PostMapping("/disabilita")
	public String disabilitazione(RedirectAttributes redirectAttrs) {
		Date now = new Date();
		for(Satellite sat : satelliteService.tuttiINonDisattivatiNonAncoraRientrati()) {
			sat.setStato(StatoSatellite.DISATTIVATO);
			sat.setDataRientro(now);
			satelliteService.aggiorna(sat);
		}
		redirectAttrs.addFlashAttribute("successMessage", "Protocollo eseguito con successo.");
		return "redirect:/home";		
	}

	@PostMapping("/launch/{idSatellite}")
	public String lanciaSatellite(@PathVariable(required = true) Long idSatellite, RedirectAttributes redirectAttrs) {

		Satellite daLanciare = satelliteService.caricaSingoloElemento(idSatellite);

		if (daLanciare.getStato() == null || daLanciare.getDataLancio() == null
				|| daLanciare.getDataLancio().after(new Date())) {
			daLanciare.setDataLancio(new Date());
			daLanciare.setStato(StatoSatellite.IN_MOVIMENTO);
			satelliteService.aggiorna(daLanciare);
			redirectAttrs.addFlashAttribute("successMessage", "Operazione eseguita correttamente");
		} else
			redirectAttrs.addFlashAttribute("errorMessage", "Impossibile lanciare satellite");
		return "redirect:/satellite";
	}

	@PostMapping("/recover/{idSatellite}")
	public String recuperaSatellite(@PathVariable(required = true) Long idSatellite, RedirectAttributes redirectAttrs) {

		Satellite daRecuperare = satelliteService.caricaSingoloElemento(idSatellite);

		if ((daRecuperare.getDataLancio() != null || daRecuperare.getDataLancio().before(new Date()))
				&& (daRecuperare.getDataRientro() == null || daRecuperare.getDataRientro().after(new Date()))) {
			daRecuperare.setDataRientro(new Date());
			daRecuperare.setStato(StatoSatellite.DISATTIVATO);
			satelliteService.aggiorna(daRecuperare);
			redirectAttrs.addFlashAttribute("successMessage", "Operazione eseguita correttamente");
		} else
			redirectAttrs.addFlashAttribute("errorMessage", "Impossibile recuperare il satellite");
		return "redirect:/satellite";
	}

	@GetMapping("/piuDiDueAnni")
	public ModelAndView listAllInOrbitaDaPiuDi2Anni() {
		ModelAndView mv = new ModelAndView();
		List<Satellite> results = satelliteService.tuttiLanciatiPiuDiDueAnniFa();
		mv.addObject("satellite_list_attribute", results);
		mv.setViewName("satellite/list");
		return mv;
	}

	@GetMapping("/disattivatiMaInOrbita")
	public ModelAndView tuttiDisattivatiMaInOrbita() {
		ModelAndView mv = new ModelAndView();
		List<Satellite> results = satelliteService.trovaTuttiDisattivatiMaInOrbita();
		mv.addObject("satellite_list_attribute", results);
		mv.setViewName("satellite/list");
		return mv;
	}

	@GetMapping("/inOrbitaDa10AnniEFissi")
	public ModelAndView tuttiInOrbitaDa10AnniEFissi() {
		ModelAndView mv = new ModelAndView();
		List<Satellite> results = satelliteService.trovaTuttiFissiInOrbitaDaDieciAnni();
		mv.addObject("satellite_list_attribute", results);
		mv.setViewName("satellite/list");
		return mv;
	}

	private boolean dateOrdinate(Satellite satellite) {
		return satellite.getDataLancio().before(satellite.getDataRientro());
	}

	private boolean dopoIlRientro(Satellite satellite, Date adesso) {
		return satellite.getDataRientro().before(adesso);
	}

	private boolean primaDelRientro(Satellite satellite, Date adesso) {
		return satellite.getDataRientro().after(adesso);
	}

	private boolean dopoIlLancio(Satellite satellite, Date adesso) {
		return satellite.getDataLancio().before(adesso);
	}

	private boolean primaDelLancio(Satellite satellite, Date adesso) {
		return satellite.getDataLancio().after(adesso);
	}

	private boolean dataLancioNullaEDataRientroNonNulla(Satellite satellite) {
		return satellite.getDataLancio() == null && satellite.getDataRientro() != null;
	}

	private String validaParametri(Satellite satellite, Date now, BindingResult result) {
		if (dataLancioNullaEDataRientroNonNulla(satellite)) {
			result.rejectValue("dataLancio", "satellite.dataLancio.isblankbutdatarientroisnot");
			return "satellite/";
		}
		if (satellite.getDataLancio() != null && satellite.getDataRientro() != null && !dateOrdinate(satellite)) {
			result.rejectValue("dataLancio", "satellite.dataLancio.dateInvertite");
			result.rejectValue("dataRientro", "satellite.dataLancio.dateInvertite");
			return "satellite/";
		}
		if ((satellite.getDataLancio() == null || primaDelLancio(satellite, now)) && satellite.getStato() != null) {
			result.rejectValue("stato", "satellite.stato.stateShouldNotExist");
			return "satellite/";
		}
		if (satellite.getDataLancio() != null && dopoIlLancio(satellite, now) && satellite.getStato() == null) {
			result.rejectValue("stato", "satellite.stato.stateShouldExist");
			return "satellite/";
		}
		return null;
	}
}
