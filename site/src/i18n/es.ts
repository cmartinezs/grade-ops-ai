export const es = {
  // Nav
  'nav.requirements':         'Requisitos',
  'nav.development':          'Desarrollo',
  'nav.technicals':           'Técnico',
  'nav.xprize':               'XPRIZE',
  'nav.contact':              'Contacto',

  // Hero
  'hero.badge':               'En desarrollo · XPRIZE AI Hackathon 2026',
  'hero.h1':                  'Assessment operations',
  'hero.h2':                  'para docentes de',
  'hero.h3':                  'programación',
  'hero.sub':                 'GradeOps AI opera el ciclo completo de evaluación con agentes de IA: actividad, rúbrica, corrección, feedback, brechas de aprendizaje y reporte. El docente aprueba; los agentes ejecutan.',
  'hero.cta.primary':         'Explorar el proyecto',
  'hero.cta.secondary':       'Ver roadmap',

  // Stat strip
  'stat.agents.label':        'Agentes especializados',
  'stat.deadline.label':      'Deadline hackathon',
  'stat.modes.label':         'Modos de evaluación',
  'stat.approval.label':      'Teacher approval',

  // Pipeline
  'pipeline.title':           'Cómo funciona',
  'pipeline.sub':             'Trece agentes especializados operan el ciclo completo de evaluación en dos modos.',
  'pipeline.open':            'Evaluación Abierta',
  'pipeline.closed':          'Evaluación Cerrada',

  // Features
  'features.title':           '¿Por qué GradeOps AI?',
  'features.f1.title':        'Teacher in control',
  'features.f1.desc':         'Los agentes sugieren. Los docentes aprueban. Ninguna acción afecta estudiantes sin autorización explícita.',
  'features.f2.title':        'Evidence first',
  'features.f2.desc':         'Cada ejecución genera un log estructurado: modelo, costo, input, output, estado de aprobación.',
  'features.f3.title':        'Full cycle ops',
  'features.f3.desc':         'De objetivo de aprendizaje a reporte docente. Sin fragmentar el flujo en herramientas distintas.',

  // Roadmap preview
  'roadmap.title':            'Roadmap',
  'roadmap.cta':              'Ver roadmap completo',
  'roadmap.p1':               'Fundación',
  'roadmap.p2':               'MVP',
  'roadmap.p3':               'Pilots',
  'roadmap.p4':               'Packaging',

  // XPRIZE banner
  'xprize.text':              'Participando en XPRIZE AI Hackathon 2026',
  'xprize.deadline':          'Deadline: Aug 17, 2026 · 1:00 PM PDT',
  'xprize.cta':               'Más información',

  // Footer
  'footer.tagline':           'AI-native assessment operations para docentes de programación.',
  'footer.built':             'Construido con Astro · Gemini · Google Cloud',

  // Subpage heroes
  'req.title':                'Requerimientos funcionales',
  'req.sub':                  'Lo que GradeOps AI debe permitir hacer — sin referencias técnicas.',
  'dev.title':                'Estado del desarrollo',
  'dev.sub':                  'Avance por fase, semana actual y deadline del hackathon.',
  'tech.title':               'Stack y arquitectura',
  'tech.sub':                 'Tecnologías, decisiones de diseño y arquitectura del sistema.',
  'contact.title':            'Colaborar',
  'contact.sub':              'El proyecto está en desarrollo activo. Si eres docente, investigador o quieres contribuir, escríbenos.',
  'contact.name':             'Nombre',
  'contact.email':            'Email',
  'contact.role':             'Tipo de colaboración',
  'contact.role.teacher':     'Docente / piloto',
  'contact.role.dev':         'Desarrollador',
  'contact.role.researcher':  'Investigador',
  'contact.role.other':       'Otro',
  'contact.message':          'Mensaje',
  'contact.send':             'Enviar',
  'xph.title':                'XPRIZE AI Hackathon',
  'xph.sub':                  'Participando en la categoría Education & Human Potential.',
  'xph.soon':                 'Próximamente',
  'xph.countdown.days':       'días',
  'xph.countdown.hours':      'horas',
  'xph.countdown.mins':       'min',
  'xph.countdown.secs':       'seg',

  // Common
  'common.collab.cta':        '¿Quieres colaborar?',
  'common.collab.link':       'Escríbenos',
} as const;

export type UIKey = keyof typeof es;
