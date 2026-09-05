"""
Navigation Engine
Validates DynamicBottomDock tabs, badges, and BackHandler navigation stack routing.
"""

from typing import List, Optional, Dict, Any

class NavigationDestination:
    STREAM = "stream"
    HUB_QUEUE = "hub_queue"
    VAULT = "vault"
    FILES = "files"
    TERMINAL = "terminal"
    SETTINGS = "settings"

    ALL = [STREAM, HUB_QUEUE, VAULT, FILES, TERMINAL, SETTINGS]

class BackHandlerNavigator:
    def __init__(self, root_destination: str = NavigationDestination.STREAM):
        self.root_destination = root_destination
        self.current_destination = root_destination
        self.screen_stack: List[str] = [root_destination]
        self.active_modal: Optional[str] = None
        self.is_player_open: bool = False
        self.app_exited: bool = False

    def navigate_to(self, destination: str):
        if destination in NavigationDestination.ALL:
            self.current_destination = destination
            if not self.screen_stack or self.screen_stack[-1] != destination:
                self.screen_stack.append(destination)
            self.active_modal = None
            self.is_player_open = False

    def open_modal(self, modal_name: str):
        self.active_modal = modal_name

    def close_modal(self):
        self.active_modal = None

    def open_player(self):
        self.is_player_open = True

    def close_player(self):
        self.is_player_open = False

    def handle_system_back(self) -> str:
        """
        Simulates Jetpack Compose BackHandler logic:
        1. If a modal is open, close the modal.
        2. Else if the video player is open, close player back to catalog.
        3. Else if screen stack has multiple tabs, pop to previous.
        4. Else if on non-stream root tab, navigate to Stream catalog root.
        5. Else if on Stream root and stack is 1, exit app safely.
        """
        if self.active_modal is not None:
            self.active_modal = None
            return "MODAL_DISMISSED"

        if self.is_player_open:
            self.is_player_open = False
            return "PLAYER_CLOSED"

        if len(self.screen_stack) > 1:
            self.screen_stack.pop()
            self.current_destination = self.screen_stack[-1]
            return f"POPPED_TO_{self.current_destination.upper()}"

        if self.current_destination != self.root_destination:
            self.current_destination = self.root_destination
            self.screen_stack = [self.root_destination]
            return f"ROUTED_TO_ROOT_{self.root_destination.upper()}"

        self.app_exited = True
        return "APP_EXITED"

