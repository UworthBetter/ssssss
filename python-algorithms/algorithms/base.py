from abc import ABC, abstractmethod
from typing import Dict, Any, Optional

class BaseAlgorithm(ABC):
    """
    Abstract base class for all AI algorithms in the system.
    Enforces a consistent interface for 'analyze' and 'detect' operations.
    """
    
    @abstractmethod
    def run(self, data: Dict[str, Any]) -> Dict[str, Any]:
        """
        Main entry point for the algorithm.
        
        Args:
            data: Input data dictionary (sensor readings, user profile, etc.)
            
        Returns:
            A dictionary containing the analysis results. 
            Should include standard fields like 'code', 'msg' where possible, 
            or specific result fields.
        """
        pass

    def validate_input(self, data: Dict[str, Any], required_fields: list) -> bool:
        """
        Basic input validation helper.
        """
        return all(field in data for field in required_fields)
