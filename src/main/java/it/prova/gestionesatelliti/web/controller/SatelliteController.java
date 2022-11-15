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

		// se ci sono errori di validazione...
		if (result.hasErrors()
				// o se le date hanno un ordine insensato...
				|| satellite.getDataRientro() != null && satellite.getDataLancio() == null
				// o se c'è una data di lancio passata ma nessuno stato OPPURE c'è uno stato ma
				// il satellite non è ancora stato lanciato...
				|| satellite.getDataLancio().before(new Date()) == (satellite.getStato() == null))
			return "satellite/insert"; //torno alla insert

		//altrimenti Inserisco
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
		
		if((s.getDataLancio()!=null && s.getDataRientro()==null))
			return "redirect:/satellite";
		if(s.getDataRientro()!=null&&(s.getDataRientro().after(new Date())||s.getDataLancio().before(new Date())))
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

		if (result.hasErrors())
			return "satellite/update";

		satelliteService.aggiorna(satellite);

		redirectAttrs.addFlashAttribute("successMessage", "Operazione eseguita correttamente");
		return "redirect:/satellite";
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

	@PostMapping("/recover/{idSatellite")
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
}
