<?php

namespace App\Controller\Api;

use App\Entity\Annonce;
use App\Entity\User;
use App\Repository\CategorieRepository;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Routing\Attribute\Route;

class AnnonceController extends AbstractController
{
    // =========================================
    // 1. GET ALL CATEGORIES
    // =========================================

    #[Route(
        '/api/categories',
        name: 'api_categories_list',
        methods: ['GET']
    )]
    public function getCategories(
        CategorieRepository $categorieRepository
    ): JsonResponse {

        $categories = $categorieRepository->findAll();

        $result = [];

        foreach ($categories as $categorie) {

            $result[] = [
                'id' => $categorie->getId(),
                'libelle' => $categorie->getLibelle()
            ];
        }

        return $this->json([
            'success' => true,
            'categories' => $result
        ]);
    }


    // =========================================
    // 2. CREATE ANNOUNCEMENT
    // =========================================

    #[Route(
        '/api/annonces/create',
        name: 'api_annonce_create',
        methods: ['POST']
    )]
    public function createAnnonce(
        Request $request,
        EntityManagerInterface $entityManager,
        UserRepository $userRepository,
        CategorieRepository $categorieRepository
    ): JsonResponse {

        $data = json_decode($request->getContent(), true);

        if (!is_array($data)) {
            return $this->json([
                'success' => false,
                'message' => 'Invalid JSON data'
            ], 400);
        }

        // Get user
        $userId = $data['user_id'] ?? null;

        $user = $userRepository->find($userId);

        if (!$user) {
            return $this->json([
                'success' => false,
                'message' => 'User not found'
            ], 404);
        }

        // Get category
        $categorieId = $data['categorie_id'] ?? null;

        $categorie = $categorieRepository->find($categorieId);

        if (!$categorie) {
            return $this->json([
                'success' => false,
                'message' => 'Category not found'
            ], 404);
        }

        // Required fields
        $titre = trim($data['titre'] ?? '');
        $description = trim($data['description'] ?? '');
        $transaction = strtoupper(
            trim($data['type_transaction'] ?? '')
        );

        $etat = strtoupper(
            trim($data['etat_objet'] ?? '')
        );

        if ($titre === '' || $description === '') {
            return $this->json([
                'success' => false,
                'message' => 'Title and description are required'
            ], 400);
        }

        // Validate transaction type
        $allowedTransactions = [
            'VENTE',
            'LOCATION',
            'PRET'
        ];

        if (!in_array($transaction, $allowedTransactions, true)) {
            return $this->json([
                'success' => false,
                'message' => 'Invalid transaction type'
            ], 400);
        }

        // Validate object condition
        $allowedConditions = [
            'NEUF',
            'TRES_BON',
            'BON',
            'MOYEN',
            'ABIME'
        ];

        if (!in_array($etat, $allowedConditions, true)) {
            return $this->json([
                'success' => false,
                'message' => 'Invalid object condition'
            ], 400);
        }

        // Handle price
        $prix = null;

        if ($transaction === 'PRET') {
            $prix = 0;
        } else {

            $prixInput = $data['prix'] ?? null;

            if ($transaction === 'VENTE' &&
                ($prixInput === null || $prixInput === '')) {

                return $this->json([
                    'success' => false,
                    'message' => 'Price is required for sale'
                ], 400);
            }

            if ($prixInput !== null && $prixInput !== '') {

                if (!is_numeric($prixInput) || (float) $prixInput < 0) {
                    return $this->json([
                        'success' => false,
                        'message' => 'Invalid price'
                    ], 400);
                }

                $prix = (float) $prixInput;
            }
        }

        // Create announcement
        $annonce = new Annonce();

        $annonce->setUtilisateur($user);
        $annonce->setCategorie($categorie);

        $annonce->setTitre($titre);
        $annonce->setDescription($description);

        $annonce->setTypeTransaction($transaction);
        $annonce->setPrix($prix);

        $annonce->setEtatObjet($etat);

        $annonce->setDisponibilite(
            $data['disponibilite'] ?? 'DISPONIBLE'
        );

        $annonce->setVille($data['ville'] ?? null);
        $annonce->setQuartier($data['quartier'] ?? null);

        if (isset($data['latitude']) && $data['latitude'] !== '') {
            $annonce->setLatitude((float) $data['latitude']);
        }

        if (isset($data['longitude']) && $data['longitude'] !== '') {
            $annonce->setLongitude((float) $data['longitude']);
        }

        $entityManager->persist($annonce);
        $entityManager->flush();

        return $this->json([
            'success' => true,
            'message' => 'Announcement created successfully',
            'annonce_id' => $annonce->getId()
        ], 201);
    }


    // =========================================
    // 3. GET ALL ANNOUNCEMENTS WITH PHOTOS
    // =========================================

    #[Route(
        '/api/annonces',
        name: 'api_annonces_list',
        methods: ['GET']
    )]
    public function getAllAnnonces(
        EntityManagerInterface $entityManager
    ): JsonResponse {

        $annonces = $entityManager
            ->getRepository(Annonce::class)
            ->findBy([], ['datePublication' => 'DESC']);

        $result = [];

        foreach ($annonces as $annonce) {

            // Photos
            $photos = [];

            foreach ($annonce->getPhotos() as $photo) {

                $photos[] = [
                    'id' => $photo->getId(),
                    'url_photo' => $photo->getUrlPhoto()
                ];
            }

            // Category
            $categorie = $annonce->getCategorie();

            // Owner
            $utilisateur = $annonce->getUtilisateur();

            $result[] = [

                'id' => $annonce->getId(),

                'titre' => $annonce->getTitre(),

                'description' => $annonce->getDescription(),

                'type_transaction' =>
                    $annonce->getTypeTransaction(),

                'prix' => $annonce->getPrix(),

                'etat_objet' =>
                    $annonce->getEtatObjet(),

                'disponibilite' =>
                    $annonce->getDisponibilite(),

                'ville' => $annonce->getVille(),

                'quartier' => $annonce->getQuartier(),

                'date_publication' =>
                    $annonce->getDatePublication()
                        ?->format('Y-m-d H:i:s'),

                'categorie' => $categorie ? [
                    'id' => $categorie->getId(),
                    'libelle' => $categorie->getLibelle()
                ] : null,

                'utilisateur' => $utilisateur ? [
                    'id' => $utilisateur->getId(),
                    'nom' => $utilisateur->getNom(),
                    'prenom' => $utilisateur->getPrenom()
                ] : null,

                'photos' => $photos
            ];
        }

        return $this->json([
            'success' => true,
            'count' => count($result),
            'annonces' => $result
        ]);
    }
}
