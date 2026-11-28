<?php

namespace App\Controller\Api;

use App\Entity\User;
use App\Entity\Localisation;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;

class UpdateLocationController extends AbstractController
{
    #[Route('/api/profile/location', name: 'app_api_profile_location', methods: ['POST'])]
    public function update(
        Request $request,
        EntityManagerInterface $entityManager
    ): JsonResponse {

        $data = json_decode($request->getContent(), true);

        if (!is_array($data)) {
            return $this->json([
                'success' => false,
                'message' => 'Invalid JSON data'
            ], 400);
        }

        $userId = $data['user_id'] ?? null;
        $ville = trim($data['ville'] ?? '');
        $quartier = trim($data['quartier'] ?? '');

        if (!$userId || !is_numeric($userId)) {
            return $this->json([
                'success' => false,
                'message' => 'User ID is required'
            ], 400);
        }

        if ($ville === '') {
            return $this->json([
                'success' => false,
                'message' => 'City is required'
            ], 400);
        }

        $user = $entityManager->getRepository(User::class)->find($userId);

        if (!$user) {
            return $this->json([
                'success' => false,
                'message' => 'User not found'
            ], 404);
        }

        $location = $user->getLocalisation();

        if (!$location) {
            $location = new Localisation();
            $entityManager->persist($location);
        }

        $location->setVille($ville);
        $location->setQuartier($quartier !== '' ? $quartier : null);

        $user->setLocalisation($location);

        $entityManager->flush();

        return $this->json([
            'success' => true,
            'message' => 'Location updated successfully',
            'location' => [
                'ville' => $location->getVille(),
                'quartier' => $location->getQuartier()
            ]
        ]);
    }
}
