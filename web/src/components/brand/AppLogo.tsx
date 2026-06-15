interface Props {
  className?: string;
}

export default function AppLogo({ className = "" }: Props) {
  return (
    <div className={`flex flex-col items-center gap-2 ${className}`}>
      <div className="flex items-center gap-3">
        {/* Mark: grading approval (checkmark) + AI agents (nodes) */}
        <svg
          width="44"
          height="44"
          viewBox="0 0 44 44"
          xmlns="http://www.w3.org/2000/svg"
          aria-hidden="true"
        >
          <defs>
            <linearGradient id="go-grad" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0%" stopColor="#6366f1" />
              <stop offset="100%" stopColor="#3b82f6" />
            </linearGradient>
          </defs>
          {/* Background */}
          <rect width="44" height="44" rx="11" fill="url(#go-grad)" />
          {/* Agent connectors (thin, behind) */}
          <line x1="12" y1="22" x2="20" y2="30" stroke="white" strokeWidth="1.5" strokeOpacity="0.35" />
          <line x1="20" y1="30" x2="33" y2="15" stroke="white" strokeWidth="1.5" strokeOpacity="0.35" />
          <line x1="12" y1="22" x2="33" y2="15" stroke="white" strokeWidth="1" strokeOpacity="0.2" strokeDasharray="2 3" />
          {/* Checkmark stroke */}
          <path
            d="M12 22 L20 30 L33 15"
            stroke="white"
            strokeWidth="3.5"
            strokeLinecap="round"
            strokeLinejoin="round"
            fill="none"
          />
          {/* Agent nodes */}
          <circle cx="12" cy="22" r="3" fill="white" fillOpacity="0.45" />
          <circle cx="33" cy="15" r="3" fill="white" fillOpacity="0.45" />
          {/* Approval node (brightest — teacher's final call) */}
          <circle cx="20" cy="30" r="3.5" fill="white" />
        </svg>

        {/* Wordmark */}
        <div className="leading-none select-none">
          <span className="text-[26px] font-bold tracking-tight text-gray-900">GradeOps</span>
          <span className="text-[26px] font-bold tracking-tight text-indigo-600"> AI</span>
        </div>
      </div>

      {/* Slogan */}
      <p className="text-[13px] tracking-widest text-gray-400 uppercase">
        Grade smarter. Teach better.
      </p>
    </div>
  );
}
